package org.onehippo.assessment;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.flat.TreeTraverser;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public final class Application {

    private static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws RepositoryException {
        String repoUrl = "rmi://localhost:1099/hipporepository";
        String username = "admin";
        char[] password = "admin".toCharArray();

        HippoRepository repository = HippoRepositoryFactory.getHippoRepository(repoUrl);
        Session session = repository.login(username, password);
        Node rootNode = session.getRootNode();

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
}
