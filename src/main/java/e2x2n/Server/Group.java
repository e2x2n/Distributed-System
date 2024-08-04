package e2x2n.Server;

import java.util.*;

public class Group {
    private String groupName;
    private Set<ServerClientHandler> participants;

    public Group(String groupName) {
        this.groupName = groupName;
        this.participants = Collections.synchronizedSet(new HashSet<>());
    }

    public String getGroupName() {
        return groupName;
    }

    public Set<ServerClientHandler> getParticipants() {
        return participants;
    }

    public void addParticipants(ServerClientHandler participant) {
        participants.add(participant);
    }

    public void removeParticipants(ServerClientHandler participant) {
        participants.remove(participant);
    }

    public void broadcastMessage(String message) {
        synchronized (participants) {
            for (ServerClientHandler member : participants) {
                member.sendMessage(message);
            }
        }
    }
}
