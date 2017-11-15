import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Paging {

    ArrayList<Process> processes = new ArrayList<>();
    LinkedList<ArrayList<Integer>> ft = new LinkedList<>();
    Queue<Integer> random_number = new LinkedList<>();
    int num_of_process, num_of_pages, num_of_frame;
    String algorithm;
    int TIME = 0;

    public Paging(int M, int P, int S, int J, int N, String R) throws FileNotFoundException {
        this.num_of_process = num_of_process(J);
        this.num_of_pages = S/P;
        this.num_of_frame = M/P;
        this.algorithm = R;

        System.out.println("The machine size is " + M);
        System.out.println("The page size is " + P);
        System.out.println("The process size is " + S);
        System.out.println("The job mix number is " + J);
        System.out.println("The number of references per process is " + N);
        System.out.println("The replacement algorithm is " + R);

        //get random number
        Scanner sc = new Scanner(new File("random-numbers.txt"));
        while (sc.hasNext()){
            int next = sc.nextInt();
            random_number.add(next);
        }

        //initialize process
        for (int i = 0; i < this.num_of_process; i++){
            Process p = new Process(this.num_of_pages, S, i+1, N, P);
            this.processes.add(p);
        }

        //build frame table
        for (int i = 0; i < this.num_of_frame; i++){
            ArrayList<Integer> f = new ArrayList<>(2); // {(Process, page), (Process page) etc}
            f.add(-1);
            f.add(-1);
            ft.add(f);
        }

        unpackJ(J);
    }

    public void driver(){
        while (!check_finish()) {
            for (Process p : this.processes) {
                for (int ref = 0; ref < 3; ref++) {
                    if (p.num_of_refer == 0){
                        break;
                    }
                    boolean hit = p.fetch();
                    TIME++;
                    if (hit) {
                        p.pt.get(p.current_refer/p.page_size).stamp = this.TIME;
                        next_refer(p);
                        continue;
                    }else {
                        pager(p);
                        next_refer(p);
                        continue;
                    }
                }
            }
        }
    }

    public void pager(Process p){
        if (this.algorithm.compareTo("lifo") == 0){
            int free_frame = check_free_frame();
            int page = p.current_refer/p.page_size;

            if (free_frame != -1){ // if there are free frame to use
                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(this.processes.indexOf(p));
                temp.add(page); //add page number
                this.ft.set(free_frame, temp);

                p.pt.get(page).frame_num = free_frame;
                p.pt.get(page).load_time = TIME;
            }else{ // no free frame to use
                ArrayList<Integer> fte = ft.peek();
                Process evicted_process = processes.get(fte.get(0));
                evicted_process.num_of_eviction++;

                evicted_process.resident_time += TIME - evicted_process.pt.get(fte.get(1)).load_time;
                evicted_process.pt.get(fte.get(1)).frame_num = -1;
                evicted_process.pt.get(fte.get(1)).load_time = -1;

                p.pt.get(page).frame_num = 0;
                p.pt.get(page).load_time = TIME;

                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(this.processes.indexOf(p));
                temp.add(page);
                this.ft.set(0, temp);
            }
        }else if (this.algorithm.compareTo("lru") == 0){
            int free_frame = check_free_frame();
            int page = p.current_refer/p.page_size;

            if (free_frame != -1){ // if there are free frame to use
                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(this.processes.indexOf(p));
                temp.add(page); //add page number
                this.ft.set(free_frame, temp);

                p.pt.get(page).frame_num = free_frame;
                p.pt.get(page).load_time = TIME;
                p.pt.get(page).stamp = TIME;

            }else{ // no free frame to use
                int victim = find_lru();
                ArrayList<Integer> fte = ft.get(victim);
                Process evicted_process = processes.get(fte.get(0));
                evicted_process.num_of_eviction++;

                evicted_process.resident_time += TIME - evicted_process.pt.get(fte.get(1)).load_time;
                evicted_process.pt.get(fte.get(1)).frame_num = -1;
                evicted_process.pt.get(fte.get(1)).load_time = -1;

                p.pt.get(page).frame_num = victim;
                p.pt.get(page).load_time = TIME;
                p.pt.get(page).stamp = TIME;

                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(this.processes.indexOf(p));
                temp.add(page);
                this.ft.set(victim, temp);
            }

        }else{
            int free_frame = check_free_frame();
            int page = p.current_refer/p.page_size;

            if (free_frame != -1){ // if there are free frame to use
                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(this.processes.indexOf(p));
                temp.add(page); //add page number
                this.ft.set(free_frame, temp);

                p.pt.get(page).frame_num = free_frame;
                p.pt.get(page).load_time = TIME;
            }else{ // no free frame to use
                int victim = this.random_number.poll() % this.num_of_frame;
                ArrayList<Integer> fte = ft.get(victim);
                Process evicted_process = processes.get(fte.get(0));
                evicted_process.num_of_eviction++;

                evicted_process.resident_time += TIME - evicted_process.pt.get(fte.get(1)).load_time;
                evicted_process.pt.get(fte.get(1)).frame_num = -1;
                evicted_process.pt.get(fte.get(1)).load_time = -1;

                p.pt.get(page).frame_num = victim;
                p.pt.get(page).load_time = TIME;

                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(this.processes.indexOf(p));
                temp.add(page);
                this.ft.set(victim, temp);
            }

        }

    }

    public int check_free_frame(){
        for (int i = this.ft.size()-1; i >= 0; i--){
            ArrayList<Integer> frame = this.ft.get(i);
            if (frame.get(0) == -1){
                return i;
            }
        }
        return -1; // no free frame
    }
    public boolean check_finish(){
        for (Process p: this.processes){
            if (p.num_of_refer != 0){
                return false;
            }
        }
        return true;
    }

    public int find_lru(){
        int min_stamp = Integer.MAX_VALUE;
        int victim = 0;

        for (int i = 0; i < this.num_of_frame; i++){
            Process p = this.processes.get(ft.get(i).get(0));
            int page = ft.get(i).get(1);
            int stamp = p.pt.get(page).stamp;
            if (stamp < min_stamp){
                min_stamp = stamp;
                victim = i;
            }
        }
        return victim;
    }

    public int num_of_process(int J){
        if (J == 1){
            return 1;
        }else{
            return 4;
        }
    }

    public void unpackJ(int J){
        if (J == 1){
            for (Process p: this.processes){
                p.A = 1;
                p.B = 0;
                p.C = 0;
            }
        }else if (J == 2){
            for (Process p: this.processes){
                p.A = 1;
                p.B = 0;
                p.C = 0;
            }
        }else if (J == 3){
            for (Process p: this.processes){
                p.A = 0;
                p.B = 0;
                p.C = 0;
            }
        }else{
            Process p1 = this.processes.get(0);
            p1.A = 0.75;
            p1.B = 0.25;
            p1.C = 0;

            Process p2 = this.processes.get(1);
            p2.A = 0.75;
            p2.B = 0;
            p2.C = 0.25;

            Process p3 = this.processes.get(2);
            p3.A = 0.75;
            p3.B = 0.125;
            p3.C = 0.125;

            Process p4 = this.processes.get(3);
            p4.A = 0.5;
            p4.B = 0.125;
            p4.C = 0.125;
        }
    }

    public void next_refer(Process p){
        double random = this.random_number.poll();
        double y = random/(Integer.MAX_VALUE + 1d);
        if (y < p.A){
            p.current_refer = (p.current_refer+1) % p.process_size;
        }else if (y < p.A + p.B){
            p.current_refer = (p.current_refer - 5 + p.process_size) % p.process_size;
        }else if (y < p.A + p.B + p.C){
            p.current_refer = (p.current_refer+4) % p.process_size;
        }else{
            p.current_refer = this.random_number.poll() % p.process_size;
        }
    }

    public void output(){
        double total_running = 0;
        double total_eviction = 0;
        int total_fault = 0;
        System.out.println("  ");
        for (Process p: this.processes){
            if (p.num_of_eviction == 0){
                total_fault += p.num_of_pagefault;
                total_running += p.resident_time;
                System.out.println("Process " + (this.processes.indexOf(p)+1) + " had " + p.num_of_pagefault + " faults");
                System.out.println("    With no evictions, the average residence is undefined.");

            }else {
                double ave_res = (double) p.resident_time / p.num_of_eviction;
                total_fault += p.num_of_pagefault;
                total_running += p.resident_time;
                total_eviction += p.num_of_eviction;
                System.out.println("Process " + (this.processes.indexOf(p)+1) + " had "
                        + p.num_of_pagefault +" faults and " + ave_res + " average residency.");
            }
        }
        if (total_eviction == 0){
            System.out.println("  ");
            System.out.println("The total number of faults is " + total_fault);
            System.out.println("    With no evictions, the average residence is undefined.");
        }else {
            System.out.println("  ");
            System.out.println("The total number of faults is " + total_fault +
                    " and the overall average residency is " + (total_running / total_eviction));
        }
    }

    public void run(){
        driver();
        output();
    }

    public static void main(String[] args) throws FileNotFoundException {
        int M, P, S, J, N;
        String R;


        M = Integer.parseInt(args[0]);
        P = Integer.parseInt(args[1]);
        S = Integer.parseInt(args[2]);
        J = Integer.parseInt(args[3]);
        N = Integer.parseInt(args[4]);
        R = args[5];

        Paging ob = new Paging(M, P, S, J, N, R);
        ob.run();
    }
}

class Process{
    int num_of_page, process_size, process_num, page_size;
    double A, B, C;
    int current_refer, num_of_refer;
    int resident_time = 0;
    int num_of_eviction = 0;
    int num_of_pagefault = 0;
    ArrayList<PTE> pt = new ArrayList<PTE>();

    public Process(int n, int s, int process_num, int N, int p){
        this.num_of_page = n;
        this.process_size = s;
        this.page_size = p;
        this.process_num = process_num;
        this.num_of_refer = N;
        this.current_refer = (this.process_num * 111) % this.process_size;

        for (int i = 0; i < num_of_page; i++){
            PTE pte = new PTE(-1, -1);
            pt.add(pte);
        }
    }

    public boolean fetch(){
        this.num_of_refer--;
        int request_page = this.current_refer/this.page_size;
        if (pt.get(request_page).load_time == -1){
            num_of_pagefault++;
            return false;
        }else{
            return true;
        }
    }
}

class PTE{
    int frame_num, load_time, stamp;

    public PTE(int f, int l){
        this.frame_num = f;
        this.load_time = l;
    }
}