package org.onehippo.assessment;

import org.apache.commons.collections.MultiHashMap;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.HashSet;
import java.util.Set;

public class NodeSynchronizer implements EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    // TODO: [AH] we could really use Guava here
    private final MultiHashMap syncMap = new MultiHashMap();

    private final ObjectContentManager ocm;

    public NodeSynchronizer(ObjectContentManager ocm) {
        this.ocm = ocm;
    }

    public void register(String path, Refreshable refreshable) {
        syncMap.put(path, refreshable);
    }

    private void batchRefresh(Set<String> toUpdate) {
        for (String path : toUpdate) {
            Object changedObject = ocm.getObject(path);

            for (Object object : syncMap.getCollection(path)) {
                ((Refreshable) object).refresh(changedObject);
            }
        }

        toUpdate.clear();
    }

    @Override
    public void onEvent(EventIterator events) {
        Set<String> toUpdate = new HashSet<>();

        while (events.hasNext()) {
            try {
                Event event = events.nextEvent();
                String path = event.getPath();  // ugly, but event.getIdentifier() is not implemented in jackrabbit
                String nodePath = path.substring(0, path.lastIndexOf('/'));

                if (syncMap.containsKey(nodePath)) {
                    toUpdate.add(nodePath);

                    if (toUpdate.size() > 4096) {
                        batchRefresh(toUpdate);
                    }
                }
            } catch (RepositoryException e) {
                LOGGER.error("NodeSynchronizer", e);
            }
        }
        batchRefresh(toUpdate);
    }
}
