
import java.util.*;
import java.io.*;

public class Linker {
    int map_length, num_of_modules;
    List<String> input;
    ArrayList<String> output = new ArrayList<>();
    Map<String,List<Integer>> use_dic = new HashMap<>();  //(address, used, defined_module)

    public Linker(String file_name) throws IOException {
        //create output file and close
        this.output.add("Symbol Table \n");
        this.output.add("\n");
        this.output.add("Memory Map \n");

        //TODO: [DONE]clean input
        this.input = input_clean(file_name);
        for (int i = 0; i < this.input.size(); i++){
            if (this.input.get(i).compareTo("") == 0){
                this.input.remove(i);
            }
        }

        try {
            this.num_of_modules = Integer.parseInt(input.get(0));
        }catch (NumberFormatException e){
            this.num_of_modules = Integer.parseInt(input.get(0).split("\\s+")[1]);
        }
        this.map_length = 0;
    }
    public int first_pass(){
        int st_counter = 1;
        int offset = 0;

        //handle symbol table and offset counting
        for (int input_line = 1; input_line<=num_of_modules*3;input_line++){
            if (input_line % 3 == 1){
                if (this.input.get(input_line).length() >= 2){
                    String[] line_content = input.get(input_line).split("\\s+");

                    //remove white space
                    //List<String> list = new ArrayList<String>(Arrays.asList(line_content));
                    //list.removeAll(Arrays.asList(""));
                    //line_content = list.toArray(new String[0]);

                    for(int i = 0; i < Integer.parseInt(line_content[0])*2; i += 2){
                        int address = Integer.parseInt(line_content[2 + i]) + offset;

                        //TODO:[DONE] add search
                        if (this.use_dic.containsKey(line_content[1+i])){
                            String error_message = "Error: This variable is multiply defined; first value used. \n";
                            this.output.add(st_counter,error_message);
                        }else {
                            Integer module_num = offset/3 + 1;
                            List<Integer> value = new ArrayList<Integer>(3);
                            value.add(address);

                            //TODO:[DONE] handle outside error
                            if (Integer.parseInt(line_content[2+i]) + 1 > this.input.get(input_line+2).split("\\s+").length - 1){
                                value.add(2);
                                value.add(module_num);
                                this.use_dic.put(line_content[1 + i], value);
                                this.output.add(st_counter,line_content[1 + i] + "=" + Integer.toString(address) + " Error: The definition of"+line_content[1+i]+"is outside module 1; zero (relative) used." + "\n");
                            }else {
                                value.add(0);
                                value.add(module_num);
                                this.use_dic.put(line_content[1 + i], value);
                                this.output.add(st_counter,line_content[1 + i] + "=" + Integer.toString(address) + "\n");
                            }

                        }
                        st_counter++;
                    }
                }else{
                    continue;
                }
            }else if (input_line % 3 == 0){
                String[] line_content = input.get(input_line).split("\\s+");

                List<String> list = new ArrayList<String>(Arrays.asList(line_content));
                list.removeAll(Arrays.asList(""));
                line_content = list.toArray(line_content);

                try {
                    offset += Integer.parseInt(line_content[0]);
                }catch (java.lang.NumberFormatException e){
                    offset += Integer.parseInt(line_content[0].split("\\s+")[0]);
                }
            }
        }
        //add memory line number to output
        for (int i = 0;i < offset; i++){
            output.add(Integer.toString(i) + ": ");
        }
        return st_counter+2;
    }
    public void second_pass(int st_counter){
        int offset = 0;

        for (int input_line = 1; input_line <= this.num_of_modules*3;input_line++){
            //handle the usage line
            if (input_line % 3 == 2 && this.input.get(input_line).length() > 1){
                String[] line_content = input.get(input_line).split("\\s+");
                for(int i = 0; i < Integer.parseInt(line_content[0]) * 2; i += 2){
                    int address = Integer.parseInt(line_content[2 + i]);
                    String key = line_content[1 + i];
                    resolve_external(key, address, input_line+1, offset, st_counter);
                }
            }else if (input_line % 3 == 0){                                              //handle type 1 2 3
                String[] line_content = input.get(input_line).split("\\s+");
                for (int i = 1; i < line_content.length; i++){
                    char[] words = line_content[i].toCharArray();
                    int address = Integer.parseInt(line_content[i].substring(0,4));
                    int type = Character.getNumericValue(words[4]);
                    if (type == 1 || type == 2){
                        int output_position = st_counter + offset + i-1;

                        //TODO: [DONE] resolve multiple definition
                        if (this.output.get(output_position).split("\\s+").length < 2) {
                            this.output.set(output_position, this.output.get(output_position) + Integer.toString(address) + "\n");
                        }else{
                            this.output.set(output_position, this.output.get(output_position).trim()+ " Error: Immediate address on use list; treated as External." + "\n");
                        }

                    }else if (type == 3){
                        address = address + offset;
                        int output_position = st_counter + offset + i-1;
                        //TODO: [DONE] resolve multiple definition
                        if (this.output.get(output_position).split("\\s+").length < 2) {
                            this.output.set(output_position, this.output.get(output_position) + Integer.toString(address) + "\n");
                        }
                    }else if (type == 4 && this.output.get(st_counter+offset+i-1).split("\\s+").length < 2){
                        int output_position = st_counter + offset + i - 1;
                        address = address + offset;
                        this.output.set(output_position,this.output.get(output_position) + Integer.toString(address) + " Error: E type address not on use chain; treated as I type. \n");
                    }
                }
                offset += Integer.parseInt(line_content[0]);
            }
        }
    }

    public List<String> input_clean(String filename) {
        List<String> clean_input = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(filename))) {
            int input_pointer = 0;
            int line_type = 1;
            while (sc.hasNext()){
                if (input_pointer == 0){
                    clean_input.add(sc.next());
                }else{
                    if (line_type == 1){
                        String next = sc.next();
                        clean_input.add(next);
                        int num_of_definition = Integer.parseInt(next);
                        for (int i = 1; i<= num_of_definition*2; i++){
                            int temp = clean_input.size() - 1;
                            clean_input.set(temp, clean_input.get(temp)+ " " + sc.next());
                        }
                        line_type = 2;
                    }else if (line_type == 2){
                        String next = sc.next();
                        clean_input.add(next);
                        int num_of_usage = Integer.parseInt(next);
                        for (int i = 1; i<= num_of_usage*2; i++){
                            int temp = clean_input.size() - 1;
                            clean_input.set(temp, clean_input.get(temp)+ " " + sc.next());
                        }
                        line_type = 3;
                    }else if (line_type == 3){
                        String next = sc.next();
                        clean_input.add(next);
                        int num_of_address = Integer.parseInt(next);
                        for (int i = 1; i<= num_of_address; i++){
                            int temp = clean_input.size() - 1;
                            clean_input.set(temp, clean_input.get(temp)+ " " + sc.next());
                        }
                        line_type = 1;
                    }
                }
                input_pointer++;
            }
        }catch (java.io.FileNotFoundException e){
            System.out.println("No such file");
        }

        return clean_input;
    }


    public void resolve_external(String key, int list_index, int line, int offset, int output_counter){
        int new_index = list_index+1;
        String[] address_line = this.input.get(line).split("\\s+");
        String string_word = address_line[new_index];
        char[] char_word = string_word.toCharArray();
        int next_address = Integer.parseInt(string_word.substring(1,4));
        boolean flag = true;
        StringBuilder external_address = new StringBuilder("000");

        //TODO: [DONE]Not defined error
        try {
            //TODO: [DONE]handle outside error
            if (this.use_dic.get(key).get(1) == 2){
                external_address = new StringBuilder(Integer.toString(list_index+offset+1));
            }else {
                external_address = new StringBuilder(Integer.toString(use_dic.get(key).get(0)));
            }

            if (Integer.parseInt(external_address.toString()) < 10){
                external_address.insert(0,"00");
            }else if (Integer.parseInt(external_address.toString()) >= 10 && Integer.parseInt(external_address.toString()) < 100){
                external_address.insert(0, "0");
            }
            use_dic.get(key).set(1,1);
        }catch (java.lang.NullPointerException e){
            flag = false;
            int output_position = output_counter + offset;
            for (int i = 1;i < address_line.length;i++){
                String word = address_line[i];
                char[] word_char = word.toCharArray();
                String op_code = Character.toString(word_char[0]);
                String new_address = op_code + "000";
                this.output.set(output_position, this.output.get(output_position)+ new_address +" Error: " + key + " is not defined; zero used."+ "\n");
                output_position++;
            }
        }
        while(flag){
            String op_code = Character.toString(char_word[0]);
            String new_address = op_code + external_address.toString();
            int output_position = output_counter + offset + list_index;
            this.output.set(output_position, this.output.get(output_position)+ new_address + "\n");

            if (next_address == 777){
                flag = false;
                break;
            }

            list_index = next_address;
            string_word = address_line[next_address + 1];
            char_word = string_word.toCharArray();
            next_address = Integer.parseInt(string_word.substring(1,4));
        }
    }
    public void run() throws FileNotFoundException {
        int st_counter = first_pass();
        second_pass(st_counter);

        for (String key: use_dic.keySet()){
            List value = use_dic.get(key);
            int used_flag = (int) value.get(1);
            if (used_flag == 0){
                int module = (int) value.get(2);
                this.output.add("Warning: "+ key + " was defined in module "+ module +" but never used. \n");
            }
        }

        PrintWriter pw = new PrintWriter("output");
        for (int i = 0; i < this.output.size(); i++){
            pw.write(this.output.get(i));
        }
        pw.close();
    }
    public static void main(String[] args) throws IOException {
        //String file_name = "input-2";
        String file_name = args[0];
        Linker lk = new Linker(file_name);
        lk.run();
    }
}
