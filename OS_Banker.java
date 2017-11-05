import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class OS_Banker {

    ArrayList<Task> task_list = new ArrayList<>();
    ArrayList<Task> pending;
    ArrayList<Integer> available_resources;
    ArrayList<Integer> holding_resources;
    int num_of_task, num_of_resources, TIME;
    int terminated_task = 0;

    public OS_Banker(String file_name) throws FileNotFoundException {

        //handle input
        //Add each task into the task_list
        //available_resources[resource type] = # of resources
        Scanner sc = new Scanner(new File(file_name));
        String[] first_line = sc.nextLine().split("\\s");
        this.num_of_task = Integer.parseInt(first_line[0]);
        this.num_of_resources = Integer.parseInt(first_line[1]);
        available_resources = new ArrayList<>(this.num_of_resources);
        holding_resources = new ArrayList<>(this.num_of_resources);
        pending = new ArrayList<>(this.num_of_task);

        for (int i = 2; i < first_line.length;i++){
            available_resources.add(Integer.parseInt(first_line[i]));
            holding_resources.add(0);
        }
        for (int i = 0; i < this.num_of_task;i++){
            Task t = new Task(this.num_of_resources, i+1);
            task_list.add(t);
        }

        while (sc.hasNext()){
            String activity = sc.next();
            int task_num = sc.nextInt();
            int b = sc.nextInt();
            int c = sc.nextInt();
            task_list.get(task_num-1).add_activity(activity, task_num, b, c);
        }
    }

    //This is for FIFO
    public void optimistic(){
        TIME = 0;

        while (terminated_task != this.terminated_task+this.num_of_task){

            //PART 1: check pending
            ArrayList<Task> new_pending = new ArrayList<>();
            for (Task t: this.pending){
                ArrayList<Integer> request = t.peekActivity();
                int resource_type = request.get(1);
                int requested = request.get(2);
                int available = available_resources.get(resource_type-1);
                if (available >= requested){
                    available_resources.set(resource_type-1, available-requested);
                    t.resource_hold.set(resource_type-1, t.resource_hold.get(resource_type-1)+requested);
                    t.removeActivity();
                    t.pending = false;
                    t.excuted = true;
                }else{
                    t.waiting++;
                    new_pending.add(t);
                }
            }
            this.pending = new_pending;


            //PART 2: execute
            for(Task t: this.task_list){
                if (t.pending || t.aborted || t.excuted || t.terminate){
                    continue;
                }
                if (t.computing != 0){
                    t.computing = t.computing - 1;
                    continue;
                }

                ArrayList<Integer> next_activity = t.peekActivity();
                // initialization
                if (next_activity.get(0) == 0){
                    t.removeActivity();
                }
                // request
                else if (next_activity.get(0) == 1){
                    int resource_type = next_activity.get(1);
                    int b = next_activity.get(2);
                    int available = available_resources.get(resource_type-1);
                     if (available >= b){
                         available_resources.set(resource_type-1, available-b);
                         t.resource_hold.set(resource_type-1, b);
                         t.removeActivity();
                     }else{
                         t.pending = true;
                         t.waiting = 1;
                         pending.add(t);
                     }
                }
                //compute
                else if (next_activity.get(0) == 2){
                    t.compute(next_activity.get(1) - 1);
                    t.removeActivity();
                }
                //release
                else if (next_activity.get(0) == 3){
                    int resource_type = next_activity.get(1);
                    int b = next_activity.get(2);
                    t.resource_hold.set(resource_type-1, t.resource_hold.get(resource_type-1)-b);
                    holding_resources.set(resource_type-1, holding_resources.get(resource_type-1)+b);
                    t.removeActivity();
                }
                //terminate
                else if (next_activity.get(0) == 4){
                    t.finish_time = TIME;
                    t.terminate = true;
                    this.num_of_task--;
                    this.terminated_task++;
                }
            }

            for (Task t: this.task_list){
                t.excuted = false;
            }
            TIME++;

            //PART 3: check deadlock
            boolean deadlock = pending.size() == this.num_of_task;
            while (deadlock && this.num_of_task != 0){

                int min = 100000;
                for (Task t: this.pending){
                    if (t.task_num < min){
                        min = t.task_num;
                    }
                }

                ArrayList<Task> temp_pending = new ArrayList<>();
                for (Task t: this.pending){
                    //abort the smallest numbered task
                    if (t.task_num == min){
                        t.aborted = true;
                        this.num_of_task--;
                        this.terminated_task++;
                        for (int i = 0; i < t.resource_hold.size(); i++){
                            this.available_resources.set(i, available_resources.get(i) + t.resource_hold.get(i));
                        }
                    }else{
                        temp_pending.add(t);
                    }
                }
                this.pending = temp_pending;

                //check after abort, if there is still a deadlock
                for (Task t: this.pending){
                    ArrayList<Integer> request = t.peekActivity();
                    int resource_type = request.get(1);
                    int requested = request.get(2);
                    int available = available_resources.get(resource_type-1);
                    if (available >= requested){
                        deadlock = false;
                    }else{
                        deadlock = true;
                    }
                }
            }

            //add released resources to the next cycle
            for (int i = 0; i < holding_resources.size(); i++){
                available_resources.set(i, available_resources.get(i)+holding_resources.get(i));
                holding_resources.set(i, 0);
            }
        }
    }

    //This is for banker algorithm
    public void banker(){
        while (terminated_task != this.terminated_task+this.num_of_task){
            //PART 1: check pending
            ArrayList<Task> new_pending = new ArrayList<>();
            for (Task t: this.pending){
                ArrayList<Integer> request = t.peekActivity();
                int resource_type = request.get(1);
                int requested = request.get(2);
                int available = available_resources.get(resource_type-1);
                if (available >= t.init_claim.get(resource_type-1)){
                    boolean flag = true;
                    for (int i = 0; i < t.init_claim.size(); i++){
                        if (t.init_claim.get(i) > this.available_resources.get(i)){
                            flag = false;
                        }
                    }
                    if (flag) {
                        available_resources.set(resource_type - 1, available - requested);
                        t.resource_hold.set(resource_type - 1, t.resource_hold.get(resource_type - 1) + requested);
                        t.removeActivity();
                        t.pending = false;
                        t.excuted = true;
                    }else{
                        t.waiting++;
                        new_pending.add(t);
                    }
                }else{
                    t.waiting++;
                    new_pending.add(t);
                }
            }
            this.pending = new_pending;

            //PART 2: execute
            for(Task t: this.task_list){
                if (t.pending || t.aborted || t.excuted || t.terminate){
                    continue;
                }
                if (t.computing != 0){
                    t.computing = t.computing - 1;
                    continue;
                }

                ArrayList<Integer> next_activity = t.peekActivity();
                if (next_activity.get(0) == 0){ // initialization
                    int resource_type = next_activity.get(1);
                    int max_claim = next_activity.get(2);
                    t.removeActivity();
                    t.init_claim.set(resource_type-1, max_claim);
                    if (max_claim > available_resources.get(resource_type-1)){
                        System.out.println(" ");
                        System.out.println("Banker aborts task "+ t.task_num+" before run begins:");
                        System.out.println("claim for resourse "+resource_type+" ("+ max_claim+") exceeds number of units present ("+available_resources.get(resource_type-1)+")");
                        t.aborted = true;
                        this.terminated_task++;
                        this.num_of_task--;
                    }
                }
                else if (next_activity.get(0) == 1){ //request
                    int resource_type = next_activity.get(1);
                    int b = next_activity.get(2);
                    int available = available_resources.get(resource_type-1);

                    if (t.init_claim.get(resource_type-1) < t.resource_hold.get(resource_type-1) + b){
                        int time = TIME +1;
                        System.out.println(" ");
                        System.out.println("During cycle "+TIME+"-"+time+" of Banker's algorithms");
                        System.out.println("    Task "+ t.task_num+"'s request exceeds its claim; aborted; "+ t.resource_hold.get(resource_type-1)+" units available next cycle");

                        t.aborted = true;
                        for (int i = 0; i < t.resource_hold.size(); i++){
                            holding_resources.set(i, t.resource_hold.get(i));
                            t.resource_hold.set(i, 0);
                        }
                        this.num_of_task--;
                        this.terminated_task++;
                        continue;
                    }

                    if (available + t.resource_hold.get(resource_type-1) >= t.init_claim.get(resource_type-1)){
                        boolean flag = true;
                        for (int i = 0; i < t.init_claim.size(); i++){
                            if (t.init_claim.get(i) > available+this.available_resources.get(i)){
                                flag = false;
                            }
                        }
                        if (flag) {
                            available_resources.set(resource_type - 1, available - b);
                            t.resource_hold.set(resource_type - 1, t.resource_hold.get(resource_type - 1) + b);
                            t.removeActivity();
                        }else{
                            t.pending = true;
                            t.waiting = 1;
                            pending.add(t);
                        }
                    }else{
                        t.pending = true;
                        t.waiting = 1;
                        pending.add(t);
                    }
                }else if (next_activity.get(0) == 2){ //compute
                    t.compute(next_activity.get(1) - 1);
                    t.removeActivity();
                }else if (next_activity.get(0) == 3){ //release
                    int resource_type = next_activity.get(1);
                    int b = next_activity.get(2);
                    t.resource_hold.set(resource_type-1, t.resource_hold.get(resource_type-1)-b);
                    holding_resources.set(resource_type-1, holding_resources.get(resource_type-1)+b);
                    t.removeActivity();
                    //t.compute_max_claim();
                }else if (next_activity.get(0) == 4){ //terminate
                    t.finish_time = TIME;
                    t.terminate = true;
                    this.num_of_task--;
                    this.terminated_task++;
                }
            }

            for (Task t: this.task_list){
                t.excuted = false;
            }
            TIME++;

            //add released resources to the next cycle
            for (int i = 0; i < holding_resources.size(); i++){
                available_resources.set(i, available_resources.get(i)+holding_resources.get(i));
                holding_resources.set(i, 0);
            }
        }
    }

    //Print on screen
    public void output(int i){ // 1: optimistic   2: banker
        int total_wait = 0;
        int total_taken = 0;
        if (i == 1){
            System.out.println("FIFO");
        }else {
            System.out.println(" ");
            System.out.println("Banker");
        }

        for (Task t: this.task_list){
            if (t.aborted){
                System.out.println("Task" + " " + t.task_num + " aborted");
                continue;
            }
            int taken = t.finish_time-t.start_time;
            total_taken = total_taken+taken;
            int wait = t.waiting;
            total_wait = total_wait + wait;
            float percentage = (float) wait/ (float) taken;
            System.out.println("Task " + t.task_num + " " + taken + " " + wait + " " + percentage);
        }

        float total_percentage = (float) total_wait / (float) total_taken;
        System.out.println("Total " + " " + total_taken + " " + total_wait + " " + total_percentage );


    }

    public static void main(String[] args) throws FileNotFoundException {
        //String file_name = "input-06";
        String file_name = args[0];

        OS_Banker optimistic = new OS_Banker(file_name);
        optimistic.optimistic();
        optimistic.output(1);

        OS_Banker banker = new OS_Banker(file_name);
        banker.banker();
        banker.output(2);
    }
}

class Task{
    int start_time = 0;
    int finish_time, waiting;
    int task_num;
    int computing = 0;
    boolean pending = false;
    boolean aborted = false;
    boolean excuted = false;
    boolean terminate = false;

    // l[0] = # of resources 1 claimed
    // the actual claim instead of the initial claim
    ArrayList<Integer> resource_hold;
    ArrayList<Integer> current_add;
    ArrayList<Integer> max_add;
    ArrayList<Integer> init_claim;

    //initiate = 0; request = 1; compute = 2; release = 3; terminate = 4
    LinkedList<ArrayList<Integer>> activity_list = new LinkedList<>();

    public Task(int num_of_resource, int tasknum){
        this.task_num = tasknum;
        resource_hold = new ArrayList<>(num_of_resource);
        current_add = new ArrayList<>(num_of_resource);
        max_add = new ArrayList<>(num_of_resource);
        init_claim = new ArrayList<>(num_of_resource);

        for (int i = 0; i < num_of_resource; i++){
            resource_hold.add(0);
            current_add.add(0);
            max_add.add(0);
            init_claim.add(0);
        }
    }

    public void compute(int time){
        this.computing = time;
    }

    public void add_activity(String activity, int a, int b, int c){
        if (activity.compareTo("initiate") == 0){
            ArrayList<Integer> temp  = new ArrayList<>(3);
            temp.add(0);
            temp.add(b);//resource type
            temp.add(c);//initial max claim
            activity_list.addLast(temp);

        }else if (activity.compareTo("request") == 0){
            ArrayList<Integer> temp = new ArrayList<>(3);
            temp.add(1);
            temp.add(b); // resource type
            temp.add(c); // # requested
            activity_list.addLast(temp);

        }else if (activity.compareTo("compute") == 0){
            ArrayList<Integer> temp = new ArrayList<>(2);
            temp.add(2);
            temp.add(b); // # of cycle needed to compute
            activity_list.addLast(temp);
        }else if (activity.compareTo("release") == 0){
            ArrayList<Integer> temp = new ArrayList<>(3);
            temp.add(3);
            temp.add(b); // resource type
            temp.add(c); // # released
            activity_list.addLast(temp);
        }else if (activity.compareTo("terminate") == 0){
            ArrayList<Integer> temp = new ArrayList<>(1);
            temp.add(4);
            activity_list.addLast(temp);
        }else{
            System.out.printf("Not supported activity: " + activity);
        }
    }

    public ArrayList peekActivity(){
        return activity_list.getFirst();
    }

    public void removeActivity(){
        activity_list.removeFirst();
    }

}
