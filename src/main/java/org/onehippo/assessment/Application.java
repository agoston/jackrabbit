package org.onehippo.assessment;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.flat.TreeTraverser;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.Iterator;

public final class Application {

    private static Logger log = LoggerFactory.getLogger(Application.class);

    private static void showcaseTraverse(final Node rootNode) throws RepositoryException {
        for (Iterator<Node> it = TreeTraverser.nodeIterator(rootNode.getNode("content")); it.hasNext(); ) {
            Node node = it.next();
            if (node.getIdentifier().startsWith("cafeface")) continue;

            System.out.println(node.getPath() + ": " + node.getIdentifier());

            for (PropertyIterator pi = node.getProperties(); pi.hasNext(); ) {
                Property property = pi.nextProperty();

                System.out.println("\t\t" + property.getName() + "[" + PropertyType.nameFromValue(property.getType()) + "] = " +
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

            System.out.println(node.getPath());
        }
    }

    public static void main(String[] args) throws RepositoryException {
        String repoUrl = "rmi://localhost:1099/hipporepository";
        String username = "admin";
        char[] password = "admin".toCharArray();

        HippoRepository repository = HippoRepositoryFactory.getHippoRepository(repoUrl);
        Session session = repository.login(username, password);
        Node rootNode = session.getRootNode();

//        showcaseTraverse(rootNode);
//        showcaseQuery(session);

        session.logout();

    }
}
