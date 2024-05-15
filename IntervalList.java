import java.util.concurrent.locks.ReentrantLock;

public class IntervalList {
    /**
     * This object keeps track of which integers are present.
     * Initially, all integers are present and the object can
     * be updated with the removal of intervals of numbers
     * and the addition of intervals of numbers where the
     * last interval applied to the object overwrites previous
     * overlapping intervals.
     */
    public Node head;
    public IntervalList() {
        head = new Node(Integer.MIN_VALUE, true);
        head.next = new Node(Integer.MAX_VALUE, false);
    }

    public void printList() {
        Node curr = head;
        while (curr != null) {
            System.out.println(curr.key + " " + curr.accept);
            curr = curr.next;
        }
    }
    public void addInterval(int start, int end, AddressData sourceData, Boolean personaNonGrata) {
        // Ranges are inclusive
        head.lock.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock.lock();
            try {
                while (curr.key < start) {
                    pred.lock.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                if (curr.next == null) {
                    return;
                }
                
                if (pred.accept == false) {
                    if (curr.key > end + 1) {
                        pred.next = new Node(start, true);
                        pred.next.next = new Node(end + 1, false);
                        pred.next.next.next = curr;
                        return;
                    } else if (curr.key >= end) {
                        curr.key = start;
                        return;
                    }
                    curr.key = start;
                    pred.lock.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }

                
                while (curr.key <= end) {
                    Node succ = curr.next;
                    succ.lock.lock();
                    //lock
                    pred.next = succ;
                    curr = succ;
                }
                if (curr.next == null) {
                    return;
                }
                if (curr.accept == true) {
                    if (curr.key > end + 1) {
                        Node node = new Node(end + 1, false);
                        node.next = curr;
                        pred.next = node;
                    } else {
                        Node succ = curr.next;
                        //lock
                        succ.lock.lock();
                        pred.next = succ;
                        curr = succ;
                    }
                    
                }
            } finally {
                curr.lock.unlock();
            }
        } finally {
            sourceData.personaNonGrata.set(personaNonGrata);
            pred.lock.unlock();
        }

    }

    public void removeInterval(int start, int end, AddressData sourceData, Boolean personaNonGrata) {
        // Ranges are inclusive
        head.lock.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock.lock();
            try {
                while (curr.key < start) {
                    pred.lock.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                if (curr.next == null) {
                    pred.next = new Node(start, false);
                    pred.next.next = new Node(end + 1, true);
                    pred.next.next.next = curr;
                    return;
                }

                if (pred.accept == true) {
                    if (curr.key > end + 1) {
                        pred.next = new Node(start, false);
                        pred.next.next = new Node(end + 1, true);
                        pred.next.next.next = curr;
                        return;
                    } else if (curr.key >= end) {
                        curr.key = start;
                        return;
                    }
                    curr.key = start;
                    pred.lock.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                
                while (curr.key <= end) {
                    Node succ = curr.next;
                    succ.lock.lock();
                    pred.next = succ;
                    curr = succ;
                }
                if (curr.next == null) {
                    pred.next = new Node(end + 1, true);
                    pred.next.next = new Node(Integer.MAX_VALUE, false);
                    return;
                } 
                if (curr.accept == false) {
                    if (curr.key > end + 1) {
                        Node node = new Node(end + 1, true);
                        node.next = curr;
                        pred.next = node;
                    } else {
                        Node succ = curr.next;
                        succ.lock.lock();
                        pred.next = succ;
                        curr = succ;
                    }
                }  

            } finally {
                curr.lock.unlock();
            }
        } finally {
            sourceData.personaNonGrata.set(personaNonGrata);
            pred.lock.unlock();
        }
    }

    /**
     * 
     * @param key an integer
     * @return true if the key is not inside some removed interval else false
     */
    public boolean contains(int key, AddressData senderData) {
        //lock
        head.lock.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock.lock();
            try {
                while (curr.key < key) {
                    pred.lock.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                if (curr.key == key) {
                    return curr.accept == true && !senderData.personaNonGrata.get();
                } else {
                    return pred.accept == true && !senderData.personaNonGrata.get();
                }
            } finally {
                curr.lock.unlock();
            }
            
        } finally {
            pred.lock.unlock();
        }
        
    }
    
}

class Node {
    int key;
    boolean accept;
    ReentrantLock lock;
    Node next;

    public Node(int key, boolean accept) {
        this.key = key;
        this.accept = accept;
        this.lock = new ReentrantLock();
    }
}

/*class IntervalListTest {
    public static void main(String[] args) {  
      IntervalList list = new IntervalList();
      list.removeInterval(-100, 100);
      list.addInterval(10, 19);
      list.printList();
      list.removeInterval(14, 15);
      list.printList();
      //list.addInterval(0, 8);
      for (int i = 0; i <= 20; i++) {
        if (list.contains(i)) {
            System.out.println(i + " is in the list");
        }
        else {
            System.out.println(i + " is not in the list");
        }
      }
  }
}*/
