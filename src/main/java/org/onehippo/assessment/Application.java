package org.onehippo.assessment;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.flat.TreeTraverser;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @version "$Id$"
 */
public final class Application {

    private static Logger log = LoggerFactory.getLogger(Application.class);

    static class PrintNodePropertiesConsumer implements Consumer<Node> {
        @Override
        public void accept(Node node) {
            try {
                System.out.print(node.getPath());
                System.out.print(": ");
                System.out.println(StringUtils.join(node.getProperties(), ", "));
            } catch (RepositoryException e) {
                throw new RuntimeException("Should not happen", e);
            }
        }
    }

    // TODO: should not have checked exceptions or should have utility methods to do this silently
    private static String getUuid(final Node node) {
        try {
            return node.getIdentifier();
        } catch (RepositoryException e) {
            throw new RuntimeException("Should not happen", e);
        }
    }


    public static void main(String[] args) throws RepositoryException {
        String repoUrl = "rmi://localhost:1099/hipporepository";
        String username = "admin";
        char[] password = "admin".toCharArray();

        HippoRepository repository = HippoRepositoryFactory.getHippoRepository(repoUrl);
        Session session = repository.login(username, password);
        Node rootNode = session.getRootNode();

        StreamSupport.stream(Spliterators.spliteratorUnknownSize(TreeTraverser.nodeIterator(rootNode), 0), false)
                .filter((node) -> getUuid(node).contains("cafeface"))
                .forEach(new PrintNodePropertiesConsumer());


    }
}
