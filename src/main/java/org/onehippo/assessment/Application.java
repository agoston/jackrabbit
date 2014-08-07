package org.onehippo.assessment;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.flat.TreeTraverser;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.manager.impl.ObjectContentManagerImpl;
import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.onehippo.assessment.domain.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public final class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static void showcaseTraverse(final Node rootNode) throws RepositoryException {
        for (Iterator<Node> it = TreeTraverser.nodeIterator(rootNode.getNode("content")); it.hasNext(); ) {
            Node node = it.next();
            if (node.getIdentifier().startsWith("cafeface")) continue;

            System.err.println(node.getPath() + ": " + node.getIdentifier());

            for (PropertyIterator pi = node.getProperties(); pi.hasNext(); ) {
                Property property = pi.nextProperty();

                System.err.println("\t\t" + property.getName() + "[" + PropertyType.nameFromValue(property.getType()) + "] = " +
                        (property.isMultiple() ? StringUtils.join(property.getValues(), ", ") : property.getValue()));
            }
        }
    }

    private static void showcaseQuery(final Session session) throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery("SELECT a.* FROM [nt:base] AS a WHERE CONTAINS(a.*, 'Files')", Query.JCR_SQL2);

        QueryResult queryResult = query.execute();

        for (NodeIterator it = queryResult.getNodes(); it.hasNext(); ) {
            Node node = it.nextNode();

            System.err.println(node.getPath());
        }
    }

    private static void showcaseObservation(Session session) throws RepositoryException, InterruptedException {
        ObservationManager observationManager = session.getWorkspace().getObservationManager();

        NodeEventListener listener = new NodeEventListener(event -> {
            try {
                System.err.println(event.getPath() + " " + formatEventType(event.getType()));
            } catch (RepositoryException e) {
                LOGGER.error("Listener", e);
            }
        });

        observationManager.addEventListener(listener, Event.NODE_ADDED | Event.NODE_MOVED | Event.NODE_REMOVED, "/content", true, null, null, false);

        System.err.println("Now waiting...");
        listener.await(2);

        observationManager.removeEventListener(listener);
    }

    private static void showcaseOCM(Session session) {
        Mapper mapper = new AnnotationMapperImpl(Arrays.asList(Book.class));
        ObjectContentManager ocm = new ObjectContentManagerImpl(session, mapper);

        Book myBook = new Book("/books/ohmy", "Mary Author", "123456789-1", "Oh, my book!");
        ocm.insert(myBook);
        ocm.save();

        Book retrieved = (Book) ocm.getObject("/books/ohmy");

        if (myBook.equals(retrieved)) System.err.println("Oh, my book was found!");
        else System.err.println("Wrong book!");
    }

    private static void showcaseSync(Session session) throws RepositoryException, InterruptedException {
        Mapper mapper = new AnnotationMapperImpl(Arrays.asList(Book.class));
        ObjectContentManager ocm = new ObjectContentManagerImpl(session, mapper);

        ObservationManager observationManager = session.getWorkspace().getObservationManager();

        NodeSynchronizer nodeSynchronizer = new NodeSynchronizer(ocm);

        Book b1 = (Book) ocm.getObject("/books/ohmy");
        Book b2 = (Book) ocm.getObject("/books/ohmy");
        Book b3 = (Book) ocm.getObject("/books/ohmy");

        nodeSynchronizer.register("/books/ohmy", b1);
        nodeSynchronizer.register("/books/ohmy", b2);
        nodeSynchronizer.register("/books/ohmy", b3);

        observationManager.addEventListener(nodeSynchronizer, Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED, "/books", true, null, null, false);

        b1.setAuthor("meh");
        ocm.update(b1);
        ocm.save();

        Thread.sleep(2000);

        System.err.println(b2.getAuthor());
        System.err.println(b3.getAuthor());

        observationManager.removeEventListener(nodeSynchronizer);
    }

    public static void main(String[] args) throws Exception {
        String repoUrl = "rmi://localhost:1099/hipporepository";
        String username = "admin";
        char[] password = "admin".toCharArray();

        HippoRepository repository = HippoRepositoryFactory.getHippoRepository(repoUrl);
        Session session = repository.login(username, password);
        Node rootNode = session.getRootNode();

//        showcaseTraverse(rootNode);
//        showcaseQuery(session);
//        showcaseObservation(session);

        if (!rootNode.hasNode("books")) {
            rootNode.addNode("books");
            session.save();
        }

        showcaseOCM(session);
        showcaseSync(session);

        session.logout();

    }

    private static final String formatEventType(int eventType) {
        switch (eventType) {
            case Event.NODE_MOVED:
                return "MOVED";
            case Event.NODE_REMOVED:
                return "REMOVED";
            case Event.NODE_ADDED:
                return "ADDED";
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static class NodeEventListener implements EventListener {
        private static final CountDownLatch DEFAULT = new CountDownLatch(0);

        private CountDownLatch observationFinished = DEFAULT;
        private final Consumer<Event> consumer;

        public NodeEventListener(Consumer<Event> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onEvent(EventIterator events) {
            while (events.hasNext()) {
                consumer.accept(events.nextEvent());
                observationFinished.countDown();
            }
        }

        public void await(int numEvents) {
            observationFinished = new CountDownLatch(numEvents);
            try {
                observationFinished.await();
            } catch (InterruptedException e) {
                LOGGER.error("Listener await", e);
            }
        }
    }
}
