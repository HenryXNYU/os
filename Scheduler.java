import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Comparator;

public class Scheduler {
    ArrayList<Integer> random_numbers = new ArrayList<>();
    ArrayList<Process> processes_list = new ArrayList<Process>();


    public Scheduler(String input_path, boolean vb_flag) throws FileNotFoundException {
        //read random numbers
        Scanner sc1 = new Scanner(new File("random-numbers.txt"));
        while (sc1.hasNext()){
            random_numbers.add(sc1.nextInt());
        }

        //read input
        Comparator<int[]> cp = new FCFS_comparitor();
        PriorityQueue<int[]> sorted = new PriorityQueue<>(cp);
        Scanner sc2 = new Scanner(new File(input_path));
        int A = -1, B = -1, C = -1, M = -1;
        int num_of_process = sc2.nextInt();
        for (int i = 1; i <= num_of_process; i++){
            int[] pair = new int[4];
            for (int j = 0; j < 4; j++){
                pair[j] = sc2.nextInt();
            }
            sorted.add(pair);
        }
        System.out.println("the sorted input is ");
        while (!sorted.isEmpty()){
            int[] pair = sorted.peek();
            A = pair[0];
            B = pair[1];
            C = pair[2];
            M = pair[3];
            System.out.print("(" + A +" "+ B +" "+ C + " "+ M + ")");
            sorted.remove(pair);
            processes_list.add(new Process(vb_flag,A, C, B, M, random_numbers));
        }
        System.out.println("\n");
    }

    public void run_FCFS(boolean vb_flag){
        ArrayList<Process> FCFS = new ArrayList<Process>();
        for (Process p: this.processes_list){
            FCFS.add(p);
        }
        FCFS_class FCFS_ob = new FCFS_class(vb_flag, FCFS);
        FCFS_ob.run();
    }

    public void run_RR(boolean vb_flag){
        ArrayList<Process> RR = new ArrayList<Process>();
        for (Process p: this.processes_list){
            RR.add(p);
        }
        RR_class RR_ob = new RR_class(vb_flag, RR);
        RR_ob.run();
    }

    public void run_SJF(boolean vb_flag){
        ArrayList<Process> SJF = new ArrayList<Process>();
        for (Process p: this.processes_list){
            SJF.add(p);
        }
        SJF_class SJF_ob = new SJF_class(vb_flag, SJF);
        SJF_ob.run();
    }

    public void run_HPRN(boolean vb_flag){
        ArrayList<Process> HPRN = new ArrayList<Process>();
        for (Process p: this.processes_list){
            HPRN.add(p);
        }
        HPRN_class HPRN_ob = new HPRN_class(vb_flag, HPRN);
        HPRN_ob.run();
    }

    public static void main(String[] args) throws FileNotFoundException {
        boolean vb_flag =false;
        String file_name;
        try {
            file_name = args[1];
            vb_flag = true;
        }catch (java.lang.ArrayIndexOutOfBoundsException e){
            file_name = args[0];
        }
        //String file_name = "input-7.txt";
        Scheduler ob_FCFS = new Scheduler(file_name, vb_flag);
        ob_FCFS.run_FCFS(vb_flag);
        Scheduler ob_RR = new Scheduler(file_name, vb_flag);
        ob_RR.run_RR(vb_flag);
        Scheduler ob_SJF = new Scheduler(file_name, vb_flag);
        ob_SJF.run_SJF(vb_flag);
        Scheduler ob_HPRN = new Scheduler(file_name, vb_flag);
        ob_HPRN.run_HPRN(vb_flag);

    }
}

class RR_class{
    ArrayList<Process> processes_list;
    ArrayList<int[]> running;
    LinkedList<int[]> ready;
    PriorityQueue<int[]> processes;
    LinkedList<int[]> blocked;
    int TIME, all_finished_time, quantum;
    boolean terminate, vb_flag;

    public RR_class(boolean vb_flag, ArrayList<Process> processes_list){
        Comparator<int[]> FCFS_comparator = new FCFS_comparitor();
        this.processes = new PriorityQueue<>(FCFS_comparator);
        this.processes_list = processes_list;
        this.running = new ArrayList<int[]>(1);
        this.ready = new LinkedList<int[]>(); //([arrival time], [index in processes+list])
        this.blocked = new LinkedList<int[]>();
        this.terminate = false;
        this.vb_flag = vb_flag;
        this.TIME = 0;
        this.quantum = 2;
        //order process following the tie rule
        this.processes = new PriorityQueue<>(FCFS_comparator); // ([arrival time], [index in process_list])
        for (Process p : this.processes_list) {
            int[] pair = new int[2];
            pair[0] = p.getArrive_time();
            pair[1] = this.processes_list.indexOf(p);
            this.processes.add(pair);
        }
    }

    public void display_summary_data(int finish_time, List<Process> processes_list){
        System.out.println("Summery data:");
        System.out.println("\tFinishing time: "+ finish_time);
        int num_of_processes = processes_list.size();
        int total_waiting = 0,total_turnaround = 0, total_io = 0, total_cpu = 0;
        for (Process p: processes_list){
            total_cpu += p.cpu_used;
            total_io += p.io_used;
            total_turnaround += p.finishing_time - p.getArrive_time();
            total_waiting += p.finishing_time - p.getArrive_time() - p.getTotal_cpu_time() - p.io_used;
        }

        System.out.println("\tCPU Utilization: " + (float) total_cpu/finish_time);
        System.out.println("\tIO Utilization: " + (float) total_io/finish_time);
        System.out.println("\tThroughput " + 100*((float)num_of_processes/finish_time) + " per 100 cycles");
        System.out.println("\tAverage turnaround time:  " + (float) total_turnaround/num_of_processes);
        System.out.println("\tAverage waiting time: " + (float) total_waiting/num_of_processes);
        System.out.println("\tNumber of processes simulated: " + processes_list.size());
        System.out.println("\tQuantum: " + quantum);
        System.out.println(" ");
    }

    public void display_process_data(){
        System.out.println("The scheduling algorithm used was RR");
        System.out.println(" ");
        int counter = 0;
        for (Process p: processes_list){
            System.out.println("Process" + counter);
            counter++;
            int A,B,C,M, finish_time,turnaround_time, waiting_time, io_time;
            A = p.getArrive_time();
            B = p.UDRI;
            C = p.getTotal_cpu_time();
            M = p.IO_parameter;
            finish_time = p.finishing_time;
            io_time = p.io_used;
            waiting_time = finish_time-A-C-io_time;
            turnaround_time = finish_time - A;
            System.out.println("\t(A,B,C,M) = (" + A +" "+ B +" "+ C + " "+ M + ")");
            System.out.println("\tFinishing time: "+finish_time);
            System.out.println("\tTurnaround time: " + turnaround_time);
            System.out.println("\tI/O time: " + io_time );
            System.out.println("\tWaiting time: " + waiting_time);
            System.out.println(" ");
        }
    }

    public void update_run_RR(LinkedList<int[]> blocked, List<int[]> running, int TIME){
        if (!running.isEmpty()){
            Process p = processes_list.get(running.get(0)[1]);
            if (p.getCpu_burst() < 1){
                if (p.total_cpu_time - p.cpu_used == 0) {
                    p.set_terminate();
                    p.finishing_time = TIME;
                    p.setState("Terminate");
                    running.remove(running.get(0));
                }else{
                    blocked.add(running.get(0));
                    p.setState("Blocked");
                    running.remove(running.get(0));
                }
            }else if (p.RR_cpu_burst == 0){
                ready.add(running.get(0));
                p.setState("Ready");
                running.remove(running.get(0));
            }
        }
    }
    public void update_block_RR(LinkedList<int[]> blocked, LinkedList<int[]> ready, List<int[]> running,int TIME){
        if (!blocked.isEmpty()){
            for (int i = 0; i < blocked.size();i++) {
                int[] first_blocked = blocked.get(i);
                Process p = processes_list.get(first_blocked[1]);
                if (p.total_cpu_time - p.cpu_used == 0 && p.io_burst == 0) {
                    p.setState("Terminate");
                    p.set_terminate();
                    p.finishing_time = TIME;
                    blocked.remove(first_blocked);
                    return;
                }
                if (p.io_burst < 1) {
                    if (running.isEmpty() && ready.isEmpty()) {
                        running.add(first_blocked);
                        processes_list.get(running.get(0)[1]).set_burst();
                        p.setState("Running");
                        blocked.remove(first_blocked);
                    } else {
                        ready.add(first_blocked);
                        p.setState("Ready");
                        blocked.remove(first_blocked);
                    }
                }
            }
        }
    }

    public void update_ready_RR(LinkedList<int[]> ready, List<int[]> running){
        if (!ready.isEmpty()) {
            int[] first_ready = ready.peek();
            Process p = processes_list.get(first_ready[1]);
            if (running.isEmpty()) {
                running.add(first_ready);
                Process temp = processes_list.get(running.get(0)[1]);
                if (temp.getCpu_burst() == 0) {
                    temp.set_burst();
                }else if (temp.getCpu_burst() >= 2){
                    temp.RR_cpu_burst = this.quantum;
                }else{
                    temp.RR_cpu_burst = temp.getCpu_burst();
                }
                p.setState("Running");
                ready.remove(first_ready);
            }
        }
    }

    public void run() {
        String message = "Before cycle " + TIME + ": ";
        for (Process p : processes_list) {
            String temp = " " + p.state + " 0 ";
            message += temp;
        }
        if (vb_flag) {
            System.out.println(message);
        }
        while (!terminate) {

            //check termination
            if (processes.isEmpty() && running.isEmpty() && blocked.isEmpty() && ready.isEmpty()) {
                terminate = true;
                all_finished_time = TIME;
                display_process_data();
                display_summary_data(all_finished_time, processes_list);
                continue;
            }

            //begining of time unit
            //add new process to the circle
            if (!processes.isEmpty()) {
                //process with same arrival time
                while (!processes.isEmpty() && processes.peek()[0] == TIME) {
                    update_ready_RR(ready, running);
                    update_block_RR(blocked, ready, running, TIME);
                    if (running.isEmpty()) {
                        running.add(processes.peek());
                        processes_list.get(running.get(0)[1]).set_burst();
                        processes_list.get(processes.peek()[1]).setState("Running");
                        processes.remove(processes.peek());
                    } else {
                        ready.add(processes.peek());
                        processes_list.get(processes.peek()[1]).setState("Ready");
                        processes.remove(processes.peek());
                    }
                }
                update_ready_RR(ready, running);
                update_block_RR(blocked, ready, running, TIME);
            } else {
                update_ready_RR(ready, running);
                update_block_RR(blocked, ready, running, TIME);
            }


            TIME++;
            message = "Before cycle " + TIME + ": ";
            for (Process p : processes_list) {
                String temp = " " + p.state;
                if (p.state.compareTo("Ready") == 0) {
                    temp += " 0 ";
                } else if (p.state.compareTo("Terminate") == 0) {
                    temp += " 0 ";
                } else if (p.state.compareTo("Running") == 0) {
                    temp += " " + p.RR_cpu_burst;
                } else if (p.state.compareTo("Blocked") == 0) {
                    temp += " " + p.io_burst;
                } else if (p.state.compareTo("Unstarted") == 0) {
                    temp += " 0 ";
                }
                message += temp;
            }
            if (vb_flag) {
                System.out.println(message);
            }
            //end of time unit
            if (!running.isEmpty()) {
                int[] pair = running.get(0);
                int index = pair[1];
                Process p = processes_list.get(index);
                p.update(1, 0);
            }
            if (!blocked.isEmpty()) {
                for (int[] b : blocked) {
                    processes_list.get(b[1]).update(0, 1);
                }
                //processes_list.get(blocked.peek()[1]).update(0,1);
            }

            update_run_RR(blocked, running, TIME);
        }
    }
}

class FCFS_class {
    ArrayList<Process> processes_list;
    ArrayList<int[]> running;
    PriorityQueue<int[]> ready, processes;
    LinkedList<int[]> blocked;
    int TIME, all_finished_time;
    boolean terminate, vb_flag;

    public FCFS_class(boolean vb_flag, ArrayList<Process> processes_list) {
        Comparator<int[]> FCFS_comparator = new FCFS_comparitor();
        this.processes = new PriorityQueue<>(FCFS_comparator);
        this.processes_list = processes_list;
        this.running = new ArrayList<int[]>(1);
        this.ready = new PriorityQueue<>(FCFS_comparator); //([arrival time], [index in processes+list])
        this.blocked = new LinkedList<int[]>();
        this.terminate = false;
        this.vb_flag = vb_flag;
        this.TIME = 0;
        //order process following the tie rule
        this.processes = new PriorityQueue<>(FCFS_comparator); // ([arrival time], [index in process_list])
        for (Process p : this.processes_list) {
            int[] pair = new int[2];
            pair[0] = p.getArrive_time();
            pair[1] = this.processes_list.indexOf(p);
            this.processes.add(pair);
        }
    }

    public void display_summary_data(int finish_time, List<Process> processes_list){
        System.out.println("Summery data:");
        System.out.println("\tFinishing time: "+ finish_time);
        int num_of_processes = processes_list.size();
        int total_waiting = 0,total_turnaround = 0, total_io = 0, total_cpu = 0;
        for (Process p: processes_list){
            total_cpu += p.cpu_used;
            total_io += p.io_used;
            total_turnaround += p.finishing_time - p.getArrive_time();
            total_waiting += p.finishing_time - p.getArrive_time() - p.getTotal_cpu_time() - p.io_used;
        }

        System.out.println("\tCPU Utilization: " + (float) total_cpu/finish_time);
        System.out.println("\tIO Utilization: " + (float) total_io/finish_time);
        System.out.println("\tThroughput " + 100*((float)num_of_processes/finish_time) + " per 100 cycles");
        System.out.println("\tAverage turnaround time:  " + (float) total_turnaround/num_of_processes);
        System.out.println("\tAverage waiting time: " + (float) total_waiting/num_of_processes);
        System.out.println("\tNumber of processes simulated: " + processes_list.size());
        System.out.println(" ");
    }

    public void display_process_data(){
        System.out.println("The scheduling algorithm used was FCFS");
        System.out.println(" ");
        int counter = 0;
        for (Process p: processes_list){
            System.out.println("Process" + counter);
            counter++;
            int A,B,C,M, finish_time,turnaround_time, waiting_time, io_time;
            A = p.getArrive_time();
            B = p.UDRI;
            C = p.getTotal_cpu_time();
            M = p.IO_parameter;
            finish_time = p.finishing_time;
            io_time = p.io_used;
            waiting_time = finish_time-A-C-io_time;
            turnaround_time = finish_time - A;
            System.out.println("\t(A,B,C,M) = (" + A +" "+ B +" "+ C + " "+ M + ")");
            System.out.println("\tFinishing time: "+finish_time);
            System.out.println("\tTurnaround time: " + turnaround_time);
            System.out.println("\tI/O time: " + io_time );
            System.out.println("\tWaiting time: " + waiting_time);
            System.out.println(" ");
        }
    }

    public void update_run_FCFS(LinkedList<int[]> blocked, List<int[]> running, int TIME){
        if (!running.isEmpty()){
            Process p = processes_list.get(running.get(0)[1]);
            if (p.getCpu_burst() < 1){
                if (p.total_cpu_time - p.cpu_used == 0) {
                    p.set_terminate();
                    p.finishing_time = TIME;
                    p.setState("Terminate");
                    running.remove(running.get(0));
                }else{
                    blocked.add(running.get(0));
                    p.setState("Blocked");
                    running.remove(running.get(0));
                }
            }
        }
    }
    public void update_block_FCFS(LinkedList<int[]> blocked, PriorityQueue<int[]> ready, List<int[]> running,int TIME){
        if (!blocked.isEmpty()){
            for (int i = 0; i < blocked.size();i++) {
                int[] first_blocked = blocked.get(i);
                Process p = processes_list.get(first_blocked[1]);
                if (p.total_cpu_time - p.cpu_used == 0 && p.io_burst == 0) {
                    p.setState("Terminate");
                    p.set_terminate();
                    p.finishing_time = TIME;
                    blocked.remove(first_blocked);
                    return;
                }
                if (p.io_burst < 1) {
                    if (running.isEmpty() && ready.isEmpty()) {
                        running.add(first_blocked);
                        processes_list.get(running.get(0)[1]).set_burst();
                        p.setState("Running");
                        blocked.remove(first_blocked);
                    } else {
                        ready.add(first_blocked);
                        p.setState("Ready");
                        blocked.remove(first_blocked);
                    }
                }
            }
        }
    }

    public void update_ready_FCFS(PriorityQueue<int[]> ready, List<int[]> running){
        if (!ready.isEmpty()) {
            int[] first_ready = ready.peek();
            Process p = processes_list.get(first_ready[1]);
            if (running.isEmpty()) {
                running.add(first_ready);
                processes_list.get(running.get(0)[1]).set_burst();
                p.setState("Running");
                ready.remove(first_ready);
            }
        }
    }


    public void run() {
        String message = "Before cycle " + TIME + ": ";
        for (Process p : processes_list) {
            String temp = " " + p.state + " ";
            message += temp;
        }
        while (!terminate) {
            if (vb_flag) {
                System.out.println(message);
            }
            //check termination
            if (processes.isEmpty() && running.isEmpty() && blocked.isEmpty() && ready.isEmpty()) {
                terminate = true;
                all_finished_time = TIME;
                display_process_data();
                display_summary_data(all_finished_time, processes_list);
                continue;
            }

            //begining of time unit
            //add new process to the circle
            if (!processes.isEmpty()) {
                //process with same arrival time
                while (!processes.isEmpty() && processes.peek()[0] == TIME) {
                    update_ready_FCFS(ready, running);
                    update_block_FCFS(blocked, ready, running, TIME);
                    if (running.isEmpty()) {
                        running.add(processes.peek());
                        processes_list.get(running.get(0)[1]).set_burst();
                        processes_list.get(processes.peek()[1]).setState("Running");
                        processes.remove(processes.peek());
                    } else {
                        ready.add(processes.peek());
                        processes_list.get(processes.peek()[1]).setState("Ready");
                        processes.remove(processes.peek());
                    }
                }
                update_ready_FCFS(ready, running);
                update_block_FCFS(blocked, ready, running, TIME);
            } else {
                update_ready_FCFS(ready, running);
                update_block_FCFS(blocked, ready, running, TIME);
            }


            TIME++;
            message = "Before cycle " + TIME + ": ";
            for (Process p : processes_list) {
                String temp = " " + p.state;
                if (p.state.compareTo("Ready") == 0) {
                    temp += " 0 ";
                } else if (p.state.compareTo("Terminate") == 0) {
                    temp += " 0 ";
                } else if (p.state.compareTo("Running") == 0) {
                    temp += " " + p.cpu_burst;
                } else if (p.state.compareTo("Blocked") == 0) {
                    temp += " " + p.io_burst;
                } else if (p.state.compareTo("Unstarted") == 0) {
                    temp += " 0 ";
                }
                message += temp;
            }

            //end of time unit
            if (!running.isEmpty()) {
                int[] pair = running.get(0);
                int index = pair[1];
                Process p = processes_list.get(index);
                p.update(1, 0);
            }
            if (!blocked.isEmpty()) {
                for (int[] b : blocked) {
                    processes_list.get(b[1]).update(0, 1);
                }
                //processes_list.get(blocked.peek()[1]).update(0,1);
            }

            update_run_FCFS(blocked, running, TIME);
        }
    }
}

class SJF_class{
    ArrayList<Process> processes_list;
    ArrayList<int[]> running;
    PriorityQueue<int[]> ready, processes;
    LinkedList<int[]> blocked;
    int TIME, all_finished_time;
    boolean terminate, vb_flag;

    public SJF_class(boolean vb_flag, ArrayList<Process> processes_list){
        Comparator<int[]> SJF_comparator = new SJF_comparitor();
        Comparator<int[]> FCFS_comparator = new FCFS_comparitor();
        this.processes_list = processes_list;
        this.running = new ArrayList<int[]>(1);
        this.ready = new PriorityQueue<int[]>(SJF_comparator); //([arrival time], [index in processes+list])
        this.blocked = new LinkedList<int[]>();
        this.terminate = false;
        this.vb_flag = vb_flag;
        this.TIME = 0;
        //order process following the tie rule
        this.processes = new PriorityQueue<>(FCFS_comparator); // ([arrival time], [index in process_list])
        for (Process p : this.processes_list) {
            int[] pair = new int[2];
            pair[0] = p.getArrive_time();
            pair[1] = this.processes_list.indexOf(p);
            this.processes.add(pair);
        }
    }

    public void display_summary_data(int finish_time, List<Process> processes_list){
        System.out.println("Summery data:");
        System.out.println("\tFinishing time: "+ finish_time);
        int num_of_processes = processes_list.size();
        int total_waiting = 0,total_turnaround = 0, total_io = 0, total_cpu = 0;
        for (Process p: processes_list){
            total_cpu += p.cpu_used;
            total_io += p.io_used;
            total_turnaround += p.finishing_time - p.getArrive_time();
            total_waiting += p.finishing_time - p.getArrive_time() - p.getTotal_cpu_time() - p.io_used;
        }

        System.out.println("\tCPU Utilization: " + (float) total_cpu/finish_time);
        System.out.println("\tIO Utilization: " + (float) total_io/finish_time);
        System.out.println("\tThroughput " + 100*((float)num_of_processes/finish_time) + " per 100 cycles");
        System.out.println("\tAverage turnaround time:  " + (float) total_turnaround/num_of_processes);
        System.out.println("\tAverage waiting time: " + (float) total_waiting/num_of_processes);
        System.out.println("\tNumber of processes simulated: " + processes_list.size());
        System.out.println(" ");
    }

    public void display_process_data(){
        System.out.println("The scheduling algorithm used was Shortest job first");
        System.out.println(" ");
        int counter = 0;
        for (Process p: processes_list){
            System.out.println("Process" + counter);
            counter++;
            int A,B,C,M, finish_time,turnaround_time, waiting_time, io_time;
            A = p.getArrive_time();
            B = p.UDRI;
            C = p.getTotal_cpu_time();
            M = p.IO_parameter;
            finish_time = p.finishing_time;
            io_time = p.io_used;
            waiting_time = finish_time-A-C-io_time;
            turnaround_time = finish_time - A;
            System.out.println("\t(A,B,C,M) = (" + A +" "+ B +" "+ C + " "+ M + ")");
            System.out.println("\tFinishing time: "+finish_time);
            System.out.println("\tTurnaround time: " + turnaround_time);
            System.out.println("\tI/O time: " + io_time );
            System.out.println("\tWaiting time: " + waiting_time);
            System.out.println(" ");
        }
    }

    public void update_run_SJF(LinkedList<int[]> blocked, List<int[]> running, int TIME){
        if (!running.isEmpty()){
            Process p = processes_list.get(running.get(0)[1]);
            if (p.getCpu_burst() < 1){
                if (p.total_cpu_time - p.cpu_used == 0) {
                    p.set_terminate();
                    p.finishing_time = TIME;
                    p.setState("Terminate");
                    running.remove(running.get(0));
                }else{
                    blocked.add(running.get(0));
                    p.setState("Blocked");
                    running.remove(running.get(0));
                }
            }
        }
    }
    public void update_block_SJF(LinkedList<int[]> blocked, PriorityQueue<int[]> ready, List<int[]> running,int TIME){
        if (!blocked.isEmpty()){
            for (int i = 0; i < blocked.size();i++) {
                int[] first_blocked = blocked.get(i);
                Process p = processes_list.get(first_blocked[1]);
                if (p.total_cpu_time - p.cpu_used == 0 && p.io_burst == 0) {
                    p.setState("Terminate");
                    p.set_terminate();
                    p.finishing_time = TIME;
                    blocked.remove(first_blocked);
                    return;
                }
                if (p.io_burst < 1) {
                    if (running.isEmpty() && ready.isEmpty()) {
                        running.add(first_blocked);
                        processes_list.get(running.get(0)[1]).set_burst();
                        p.setState("Running");
                        blocked.remove(first_blocked);
                    } else {
                        int[] temp = new int[3];
                        temp[0] = first_blocked[0];
                        temp[1] = p.getTotal_cpu_time()-p.cpu_used;
                        temp[2] = first_blocked[1];
                        ready.add(temp);
                        p.setState("Ready");
                        blocked.remove(first_blocked);
                    }
                }
            }
        }
    }

    public void update_ready_SJF(PriorityQueue<int[]> ready, List<int[]> running){
        if (!ready.isEmpty()) {
            int[] first_ready = ready.peek();
            Process p = processes_list.get(first_ready[2]);
            if (running.isEmpty()) {
                int[] temp = new int[2];
                temp[0] = first_ready[0];
                temp[1] = first_ready[2];
                running.add(temp);
                processes_list.get(running.get(0)[1]).set_burst();
                p.setState("Running");
                ready.remove(first_ready);
            }else{
                Comparator<int[]> c = new SJF_comparitor();
                PriorityQueue<int[]> temp_queue = new PriorityQueue<>(c);
                int[] temp = new int[3];
                temp[0] = running.get(0)[0];
                temp[1] = (int) calculate_time(processes_list.get(running.get(0)[1]));
                temp[2] = running.get(0)[1];
                temp_queue.add(temp);
                temp_queue.add(ready.peek());

                if (temp_queue.peek() != temp){
                    processes_list.get(running.get(0)[1]).setState("Ready");
                    running.remove(running.get(0));
                    ready.add(temp);
                    update_ready_SJF(ready, running);
                }
            }
        }
    }

    public int calculate_time(Process p){
        int result = p.getTotal_cpu_time() -p.cpu_used;
        return result;
    }

    public void run() {
        String message = "Before cycle " + TIME + ": ";
        for (Process p : processes_list) {
            String temp = " " + p.state + " 0 ";
            message += temp;
        }
        if (vb_flag) {
            System.out.println(message);
        }
        while (!terminate) {
            //check termination
            if (processes.isEmpty() && running.isEmpty() && blocked.isEmpty() && ready.isEmpty()) {
                terminate = true;
                all_finished_time = TIME;
                display_process_data();
                display_summary_data(all_finished_time, processes_list);
                continue;
            }

            //begining of time unit
            //add new process to the circle
            if (!processes.isEmpty()) {
                //process with same arrival time
                while (!processes.isEmpty() && processes.peek()[0] == TIME) {
                    update_block_SJF(blocked, ready, running, TIME);
                    update_ready_SJF(ready, running);

                    Process first_process = processes_list.get(processes.peek()[1]);
                    if (!running.isEmpty() && calculate_time(processes_list.get(running.get(0)[1])) > calculate_time(first_process)) {
                        int[] temp = new int[3];
                        temp[0] = running.get(0)[0];
                        temp[1] = (int) calculate_time(processes_list.get(running.get(0)[1]));
                        temp[2] = running.get(0)[1];
                        processes_list.get(running.get(0)[1]).setState("Ready");
                        ready.add(temp);
                        running.remove(running.get(0));
                    }

                    if (running.isEmpty()) {
                        running.add(processes.peek());
                        processes_list.get(running.get(0)[1]).set_burst();
                        processes_list.get(processes.peek()[1]).setState("Running");
                        processes.remove(processes.peek());
                    } else {
                        int[] temp = new int[3];
                        temp[0] = processes.peek()[0];
                        temp[1] = processes_list.get(processes.peek()[1]).getTotal_cpu_time()-processes_list.get(processes.peek()[1]).getcpu_used();
                        temp[2] = processes.peek()[1];
                        ready.add(temp);
                        processes_list.get(processes.peek()[1]).setState("Ready");
                        processes.remove(processes.peek());
                    }
                }
                update_block_SJF(blocked, ready, running, TIME);
                update_ready_SJF(ready, running);
            } else {
                update_block_SJF(blocked, ready, running, TIME);
                update_ready_SJF(ready, running);
            }


            TIME++;
            message = "Before cycle " + TIME + ": ";
            for (Process p : processes_list) {
                String temp = " " + p.state;
                if (p.state.compareTo("Ready") == 0) {
                    temp += " 0 ";
                } else if (p.state.compareTo("Terminate") == 0) {
                    temp += " 0 ";
                } else if (p.state.compareTo("Running") == 0) {
                    temp += " " + p.cpu_burst;
                } else if (p.state.compareTo("Blocked") == 0) {
                    temp += " " + p.io_burst;
                } else if (p.state.compareTo("Unstarted") == 0) {
                    temp += " 0 ";
                }
                message += temp;
            }
            if (vb_flag) {
                System.out.println(message);
            }
            //end of time unit
            if (!running.isEmpty()) {
                int[] pair = running.get(0);
                int index = pair[1];
                Process p = processes_list.get(index);
                p.update(1, 0);
            }
            if (!blocked.isEmpty()) {
                for (int[] b : blocked) {
                    processes_list.get(b[1]).update(0, 1);
                }
            }
            update_run_SJF(blocked, running, TIME);
        }
    }
}

class HPRN_class{
    ArrayList<Process> processes_list;
    ArrayList<int[]> running;
    PriorityQueue<float[]> ready;
    PriorityQueue<int[]>processes;
    LinkedList<int[]> blocked;
    int TIME, all_finished_time;
    boolean terminate, vb_flag;

    public HPRN_class(boolean vb_flag, ArrayList<Process> processes_list){
        Comparator<int[]> FCFS_comparator = new FCFS_comparitor();
        Comparator<float[]> HPRN_comparator = new HPRN_comparitor();
        this.processes = new PriorityQueue<>(FCFS_comparator);
        this.processes_list = processes_list;
        this.running = new ArrayList<int[]>(1);
        this.ready = new PriorityQueue<float[]>(HPRN_comparator); //([arrival time], [index in processes+list])
        this.blocked = new LinkedList<int[]>();
        this.terminate = false;
        this.vb_flag = vb_flag;
        this.TIME = 0;
        //order process following the tie rule
        this.processes = new PriorityQueue<>(FCFS_comparator); // ([arrival time], [index in process_list])
        for (Process p : this.processes_list) {
            int[] pair = new int[2];
            pair[0] = p.getArrive_time();
            pair[1] = this.processes_list.indexOf(p);
            this.processes.add(pair);
        }
    }
    public void display_summary_data(int finish_time, List<Process> processes_list){
        System.out.println("Summery data:");
        System.out.println("\tFinishing time: "+ finish_time);
        int num_of_processes = processes_list.size();
        int total_waiting = 0,total_turnaround = 0, total_io = 0, total_cpu = 0;
        for (Process p: processes_list){
            total_cpu += p.cpu_used;
            total_io += p.io_used;
            total_turnaround += p.finishing_time - p.getArrive_time();
            total_waiting += p.finishing_time - p.getArrive_time() - p.getTotal_cpu_time() - p.io_used;
        }

        System.out.println("\tCPU Utilization: " + (float) total_cpu/finish_time);
        System.out.println("\tIO Utilization: " + (float) total_io/finish_time);
        System.out.println("\tThroughput " + 100*((float)num_of_processes/finish_time) + " per 100 cycles");
        System.out.println("\tAverage turnaround time:  " + (float) total_turnaround/num_of_processes);
        System.out.println("\tAverage waiting time: " + (float) total_waiting/num_of_processes);
        System.out.println("\tNumber of processes simulated: " + processes_list.size());
        System.out.println(" ");
    }

    public void display_process_data(){
        System.out.println("The scheduling algorithm used was Highest Penalty Ratio Next");
        System.out.println(" ");
        int counter = 0;
        for (Process p: processes_list){
            System.out.println("Process" + counter);
            counter++;
            int A,B,C,M, finish_time,turnaround_time, waiting_time, io_time;
            A = p.getArrive_time();
            B = p.UDRI;
            C = p.getTotal_cpu_time();
            M = p.IO_parameter;
            finish_time = p.finishing_time;
            io_time = p.io_used;
            waiting_time = finish_time-A-C-io_time;
            turnaround_time = finish_time - A;
            System.out.println("\t(A,B,C,M) = (" + A +" "+ B +" "+ C + " "+ M + ")");
            System.out.println("\tFinishing time: "+finish_time);
            System.out.println("\tTurnaround time: " + turnaround_time);
            System.out.println("\tI/O time: " + io_time );
            System.out.println("\tWaiting time: " + waiting_time);
            System.out.println(" ");
        }
    }

    public void update_run_HPRN(LinkedList<int[]> blocked, List<int[]> running, int TIME){
        if (!running.isEmpty()){
            Process p = processes_list.get(running.get(0)[1]);
            if (p.getCpu_burst() < 1){
                if (p.total_cpu_time - p.cpu_used == 0) {
                    p.set_terminate();
                    p.finishing_time = TIME;
                    p.setState("Terminate");
                    running.remove(running.get(0));
                }else{
                    blocked.add(running.get(0));
                    p.setState("Blocked");
                    running.remove(running.get(0));
                }
            }
        }
    }
    public void update_block_HPRN(LinkedList<int[]> blocked, PriorityQueue<float[]> ready, List<int[]> running,int TIME){
        if (!blocked.isEmpty()){
            for (int i = 0; i < blocked.size();i++) {
                int[] first_blocked = blocked.get(i);
                Process p = processes_list.get(first_blocked[1]);
                if (p.total_cpu_time - p.cpu_used == 0 && p.io_burst == 0) {
                    p.setState("Terminate");
                    p.set_terminate();
                    p.finishing_time = TIME;
                    blocked.remove(first_blocked);
                    return;
                }
                if (p.io_burst < 1) {
                    if (running.isEmpty() && ready.isEmpty()) {
                        running.add(first_blocked);
                        processes_list.get(running.get(0)[1]).set_burst();
                        p.setState("Running");
                        blocked.remove(first_blocked);
                    } else {
                        float[] temp = new float[3];
                        temp[0] = first_blocked[0];
                        temp[1] = p.penalty_ratio;
                        temp[2] = first_blocked[1];
                        ready.add(temp);
                        p.setState("Ready");
                        blocked.remove(first_blocked);
                    }
                }
            }
        }
    }

    public void update_ready_HPRN(PriorityQueue<float[]> ready, List<int[]> running){
        if (!ready.isEmpty()) {
            float[] first_ready = ready.peek();
            Process p = processes_list.get((int) first_ready[2]);
            if (running.isEmpty()) {
                int[] temp = new int[2];
                temp[0] = (int) first_ready[0];
                temp[1] = (int) first_ready[2];
                running.add(temp);
                processes_list.get(running.get(0)[1]).set_burst();
                p.setState("Running");
                ready.remove(first_ready);
            }else{
                Comparator<float[]> c = new HPRN_comparitor();
                PriorityQueue<float[]> temp_queue = new PriorityQueue<float[]>(c);
                float[] temp = new float[3];
                temp[0] = running.get(0)[0];
                temp[1] = processes_list.get(running.get(0)[1]).penalty_ratio;
                temp[2] = running.get(0)[1];
                temp_queue.add(temp);
                temp_queue.add(ready.peek());

                if (temp_queue.peek() != temp){
                    processes_list.get(running.get(0)[1]).setState("Ready");
                    running.remove(running.get(0));
                    ready.add(temp);
                    update_ready_HPRN(ready, running);
                }
            }
        }
    }

    public float calculate_penalty(int TIME, Process p){
        float result;
        if (p.cpu_used == 0){
            result = (float) TIME - p.arrive_time;
        }else{
            result = (TIME - p.arrive_time)/ (float) (p.cpu_used);
        }
        p.penalty_ratio = result;
        return result;
    }

    public void run() {
        String message = "Before cycle " + TIME + ": ";
        for (Process p : processes_list) {
            String temp = " " + p.state + " ";
            message += temp;
        }
        while (!terminate) {
            if (vb_flag) {
                System.out.println(message);
            }

            for (Process p: processes_list){
                if (p.arrive_time <= TIME) {
                    calculate_penalty(TIME, p);
                }
            }
            for (float[] p:ready){
                p[1] = processes_list.get((int) p[2]).penalty_ratio;
            }
            //check termination
            if (processes.isEmpty() && running.isEmpty() && blocked.isEmpty() && ready.isEmpty()) {
                terminate = true;
                all_finished_time = TIME;
                display_process_data();
                display_summary_data(all_finished_time, processes_list);
                continue;
            }

            //begining of time unit
            //add new process to the circle
            if (!processes.isEmpty()) {
                //process with same arrival time
                while (!processes.isEmpty() && processes.peek()[0] == TIME) {
                    update_block_HPRN(blocked, ready, running, TIME);
                    update_ready_HPRN(ready, running);

                    Process first_process = processes_list.get(processes.peek()[1]);
                    if (!running.isEmpty() && processes_list.get(running.get(0)[1]).penalty_ratio < first_process.penalty_ratio) {
                        float[] temp = new float[3];
                        temp[0] = running.get(0)[0];
                        temp[1] = processes_list.get(running.get(0)[1]).penalty_ratio;
                        temp[2] = running.get(0)[1];
                        processes_list.get(running.get(0)[1]).setState("Ready");
                        ready.add(temp);
                        running.remove(running.get(0));
                    }
                    if (running.isEmpty()) {
                        running.add(processes.peek());
                        processes_list.get(running.get(0)[1]).set_burst();
                        processes_list.get(processes.peek()[1]).setState("Running");
                        processes.remove(processes.peek());
                    } else {
                        Process p = processes_list.get(processes.peek()[1]);
                        float[] temp = new float[3];
                        temp[0] = processes.peek()[0];
                        temp[1] = p.penalty_ratio;
                        temp[2] = processes.peek()[1];
                        ready.add(temp);
                        processes_list.get(processes.peek()[1]).setState("Ready");
                        processes.remove(processes.peek());
                    }
                }
                update_block_HPRN(blocked, ready, running, TIME);
                update_ready_HPRN(ready, running);
            } else {
                update_block_HPRN(blocked, ready, running, TIME);
                update_ready_HPRN(ready, running);
            }


            //TIME++;
            message = "Before cycle " + (TIME+1) + ": ";
            for (Process p : processes_list) {
                String temp = " " + p.state;
                if (p.state.compareTo("Ready") == 0) {
                    temp += " 0 ";
                } else if (p.state.compareTo("Terminate") == 0) {
                    temp += " 0 ";
                } else if (p.state.compareTo("Running") == 0) {
                    temp += " " + p.cpu_burst;
                } else if (p.state.compareTo("Blocked") == 0) {
                    temp += " " + p.io_burst;
                } else if (p.state.compareTo("Unstarted") == 0) {
                    temp += " 0 ";
                }
                message += temp;
            }

            //end of time unit
            if (!running.isEmpty()) {
                int[] pair = running.get(0);
                int index = pair[1];
                Process p = processes_list.get(index);
                p.update(1, 0);
            }
            if (!blocked.isEmpty()) {
                for (int[] b : blocked) {
                    processes_list.get(b[1]).update(0, 1);
                }
                //processes_list.get(blocked.peek()[1]).update(0,1);
            }
            TIME++;
            update_run_HPRN(blocked, running, TIME);
        }
    }
}

class Process{
    String state = "Unstarted";
    final int arrive_time, quantum = 2;
    int finishing_time;
    final int total_cpu_time;
    int cpu_burst, RR_cpu_burst;
    int io_burst;
    float penalty_ratio;
    final int UDRI;
    final int IO_parameter;
    int cpu_used, io_used; // IF remaing time = 0, means it is pending
    boolean vb_flag;
    boolean terminate;
    ArrayList<Integer> random_numbers;

    public Process(boolean vb_flag, int arrive_time, int total_CPU_time, int UDRI_interval, int IO_parameter, ArrayList<Integer> random_numbers){
        this.arrive_time = arrive_time;
        this.total_cpu_time = total_CPU_time;
        this.vb_flag = vb_flag;
        this.UDRI = UDRI_interval;
        this.IO_parameter = IO_parameter;
        this.random_numbers = random_numbers;
    }

    public int randomOS(boolean vb_flag, int UDRI_interval,int total_cpu_time, ArrayList<Integer> random_numbers){
        Random rd = new Random();
        int x = rd.nextInt(random_numbers.size());
        x = 1 + (x % UDRI_interval);
        //System.out.println("Find burst when choosing ready process to run" + " "+x);
        if (vb_flag){
            System.out.println("Find burst when choosing ready process to run" + " "+x);
        }
        return x;
    }

    public void update(int CPU, int IO){
        //return 0: not finished ;  return 1: process finished
        this.cpu_used += CPU * 1;
        this.io_used += IO * 1;
        this.io_burst -= IO * 1;
        this.cpu_burst -= CPU * 1;
        this.RR_cpu_burst -= CPU * 1;
    }

    public void set_burst(){
        int randomOS = randomOS(this.vb_flag, this.UDRI, this.total_cpu_time, this.random_numbers);
        if (randomOS > this.total_cpu_time-this.cpu_used){
            this.cpu_burst = this.total_cpu_time-this.cpu_used;
            if (this.cpu_burst >= this.quantum){
                this.RR_cpu_burst = this.quantum;
            }else {
                this.RR_cpu_burst = this.cpu_burst;
            }
            //this.io_burst = this.cpu_burst * this.IO_parameter;
        }else{
            this.cpu_burst = randomOS;
            if (this.cpu_burst >= this.quantum){
                this.RR_cpu_burst = this.quantum;
            }else {
                this.RR_cpu_burst = this.cpu_burst;
            }
            this.io_burst = this.IO_parameter * this.cpu_burst;
        }
    }

    public void setState(String new_state){
        this.state = new_state;
    }

    public void set_terminate(){
        this.terminate = true;
    }

    public int getArrive_time() {
        return arrive_time;
    }

    public int getTotal_cpu_time() {
        return total_cpu_time;
    }

    public int getCpu_burst() {
        return cpu_burst;
    }

    public int getIo_burst() {
        return io_burst;
    }

    public int getcpu_used() {
        return cpu_used;
    }

    public int getio_used() {
        return io_used;
    }

}

class FCFS_comparitor implements Comparator<int[]>{

    @Override
    public int compare(int[] o1, int[] o2) {
        if (o1[0] >= o2[0]){
            return 1;
        }else{
            return -1;
        }
    }
}

class SJF_comparitor implements Comparator<int[]>{

    @Override
    public int compare(int[] o1, int[] o2) {
        if (o1[1] > o2[1]){
            return 1;
        }else if (o1[1] == o2[1]){
            if (o1[0]  >= o2[0]){
                return 1;
            }else{
                return -1;
            }
        }else{
            return -1;
        }
    }
}

class HPRN_comparitor implements Comparator<float[]>{

    @Override
    public int compare(float[] o1, float[] o2) {
        if (o1[1] > o2[1]){
            return -1;
        }else if (o1[1] == o2[1]){
            if (o1[0] > o2[0]){
                return 1;
            }else if (o1[0] == o2[0]){
                if (o1[2] > o2[2]){
                    return 1;
                }else{
                    return -1;
                }
            } else{
                return -1;
            }
        }else{
            return 1;
        }
    }
}


package java.util;

public class PriorityQueue<E> extends AbstractQueue<E>
    implements java.io.Serializable {
    private static final long serialVersionUID = -7720805057305804111L;
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    private transient Object[] queue;
    private int size = 0;
    private final Comparator<? super E> comparator;
    private transient int modCount = 0;
    public PriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    public PriorityQueue(int initialCapacity,
                         Comparator<? super E> comparator) {
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.queue = new Object[initialCapacity];
        this.comparator = comparator;
    }

    public PriorityQueue(Collection<? extends E> c) {
        initFromCollection(c);
        if (c instanceof SortedSet)
            comparator = (Comparator<? super E>)
                ((SortedSet<? extends E>)c).comparator();
        else if (c instanceof PriorityQueue)
            comparator = (Comparator<? super E>)
                ((PriorityQueue<? extends E>)c).comparator();
        else {
            comparator = null;
            heapify();
        }
    }

    public PriorityQueue(PriorityQueue<? extends E> c) {
        comparator = (Comparator<? super E>)c.comparator();
        initFromCollection(c);
    }

    public PriorityQueue(SortedSet<? extends E> c) {
        comparator = (Comparator<? super E>)c.comparator();
        initFromCollection(c);
    }

    private void initFromCollection(Collection<? extends E> c) {
        Object[] a = c.toArray();
        // If c.toArray incorrectly doesn't return Object[], copy it.
        if (a.getClass() != Object[].class)
            a = Arrays.copyOf(a, a.length, Object[].class);
        queue = a;
        size = a.length;
    }

    private void grow(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        int oldCapacity = queue.length;
        // Double size if small; else grow by 50%
        int newCapacity = ((oldCapacity < 64)?
                           ((oldCapacity + 1) * 2):
                           ((oldCapacity / 2) * 3));
        if (newCapacity < 0) // overflow
            newCapacity = Integer.MAX_VALUE;
        if (newCapacity < minCapacity)
            newCapacity = minCapacity;
        queue = Arrays.copyOf(queue, newCapacity);
    }

    public boolean add(E e) {
        return offer(e);
    }

    public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        modCount++;
        int i = size;
        if (i >= queue.length)
            grow(i + 1);
        size = i + 1;
        if (i == 0)
            queue[0] = e;
        else
            siftUp(i, e);
        return true;
    }

    public E peek() {
        if (size == 0)
            return null;
        return (E) queue[0];
    }

    private int indexOf(Object o) {
        if (o != null) {
            for (int i = 0; i < size; i++)
                if (o.equals(queue[i]))
                    return i;
        }
        return -1;
    }

    public boolean remove(Object o) {
        int i = indexOf(o);
        if (i == -1)
            return false;
        else {
            removeAt(i);
            return true;
        }
    }
    
    boolean removeEq(Object o) {
        for (int i = 0; i < size; i++) {
            if (o == queue[i]) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }

    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public Object[] toArray() {
        return Arrays.copyOf(queue, size);
    }

    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(queue, size, a.getClass());
        System.arraycopy(queue, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }


    public Iterator<E> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<E> {
        private int cursor = 0;
        private int lastRet = -1;
        private ArrayDeque<E> forgetMeNot = null;
        private E lastRetElt = null;
        private int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor < size ||
                (forgetMeNot != null && !forgetMeNot.isEmpty());
        }

        public E next() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (cursor < size)
                return (E) queue[lastRet = cursor++];
            if (forgetMeNot != null) {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if (lastRetElt != null)
                    return lastRetElt;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (lastRet != -1) {
                E moved = PriorityQueue.this.removeAt(lastRet);
                lastRet = -1;
                if (moved == null)
                    cursor--;
                else {
                    if (forgetMeNot == null)
                        forgetMeNot = new ArrayDeque<E>();
                    forgetMeNot.add(moved);
                }
            } else if (lastRetElt != null) {
                PriorityQueue.this.removeEq(lastRetElt);
                lastRetElt = null;
            } else {
                throw new IllegalStateException();
            }
            expectedModCount = modCount;
        }
    }

    public int size() {
        return size;
    }

    
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++)
            queue[i] = null;
        size = 0;
    }

    public E poll() {
        if (size == 0)
            return null;
        int s = --size;
        modCount++;
        E result = (E) queue[0];
        E x = (E) queue[s];
        queue[s] = null;
        if (s != 0)
            siftDown(0, x);
        return result;
    }

    private E removeAt(int i) {
        assert i >= 0 && i < size;
        modCount++;
        int s = --size;
        if (s == i) // removed last element
            queue[i] = null;
        else {
            E moved = (E) queue[s];
            queue[s] = null;
            siftDown(i, moved);
            if (queue[i] == moved) {
                siftUp(i, moved);
                if (queue[i] != moved)
                    return moved;
            }
        }
        return null;
    }

    private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x);
        else
            siftUpComparable(k, x);
    }

    private void siftUpComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (key.compareTo((E) e) >= 0)
                break;
            queue[k] = e;
            k = parent;
        }
        queue[k] = key;
    }

    private void siftUpUsingComparator(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (comparator.compare(x, (E) e) >= 0)
                break;
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    private void siftDown(int k, E x) {
        if (comparator != null)
            siftDownUsingComparator(k, x);
        else
            siftDownComparable(k, x);
    }

    private void siftDownComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>)x;
        int half = size >>> 1;        // loop while a non-leaf
        while (k < half) {
            int child = (k << 1) + 1; // assume left child is least
            Object c = queue[child];
            int right = child + 1;
            if (right < size &&
                ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0)
                c = queue[child = right];
            if (key.compareTo((E) c) <= 0)
                break;
            queue[k] = c;
            k = child;
        }
        queue[k] = key;
    }

    private void siftDownUsingComparator(int k, E x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = queue[child];
            int right = child + 1;
            if (right < size &&
                comparator.compare((E) c, (E) queue[right]) > 0)
                c = queue[child = right];
            if (comparator.compare(x, (E) c) <= 0)
                break;
            queue[k] = c;
            k = child;
        }
        queue[k] = x;
    }

    private void heapify() {
        for (int i = (size >>> 1) - 1; i >= 0; i--)
            siftDown(i, (E) queue[i]);
    }

    public Comparator<? super E> comparator() {
        return comparator;
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // Write out element count, and any hidden stuff
        s.defaultWriteObject();

        // Write out array length, for compatibility with 1.5 version
        s.writeInt(Math.max(2, size + 1));

        // Write out all elements in the "proper order".
        for (int i = 0; i < size; i++)
            s.writeObject(queue[i]);
    }


    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in (and discard) array length
        s.readInt();

        queue = new Object[size];

        // Read in all elements.
        for (int i = 0; i < size; i++)
            queue[i] = s.readObject();

        // Elements are guaranteed to be in "proper order", but the
        // spec has never explained what that might be.
        heapify();
    }
}
