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

        // TODO: have a runtimeexception version of Node; we can't even put the nodeId in an exception message like this
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(TreeTraverser.nodeIterator(rootNode), 0), true)
                .filter(node -> {
                    try {
                        return node.getIdentifier().contains("cafe");   // TODO: confirm that uuid is forced lowercase
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forEach(node -> {
                    try {
                        System.out.println(node.getPath() + ": " + node.getIdentifier());
                        for (PropertyIterator it = node.getProperties(); it.hasNext(); ) {
                            Property property = it.nextProperty();
                            System.out.println("\t\t" + property.getName() + " = " + StringUtils.join(property.getValues(), ", "));
                        }
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                });


    }
}
