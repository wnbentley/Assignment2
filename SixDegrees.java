import java.io.FileReader;
import java.io.Reader;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class SixDegrees {

    static Hashtable<String, LinkedList<String>> graph = new Hashtable<>(); //create hashtable representation of graph

    public static void main(String[] args) {
        //read in csv file and parse JSON
        try {
            Reader reader = new FileReader(args[0]);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
            JSONParser jsonParser = new JSONParser();
            int movies = 0;

            for (CSVRecord csvRecord : csvParser) {

                if (movies > 0) {
                    String castJSON = csvRecord.get(2);
                    Object object = jsonParser.parse(castJSON);
                    JSONArray jsonArray = (JSONArray) object;
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        String actor = ((String) jsonObject.get("name")).toLowerCase(); //get as lower case
                        if (!graph.containsKey(actor)) { //if graph doesn't have actor, put em in.
                            graph.put(actor, new LinkedList<>());
                        }
                        for (Object item : jsonArray) { // loop through and add previous actors connections and add them to graph if needed
                            JSONObject data = (JSONObject) item;
                            String actor2 = ((String) data.get("name")).toLowerCase(); //get as lower case.
                            if (!graph.containsKey(actor2)) {
                                graph.put(actor2, new LinkedList<>());
                            }
                            addEdge(actor, actor2); //add edge between actors
                        }
                    }
                }
                ++movies;
            }
            csvParser.close();
        } catch (Exception e) {
            System.out.println("File " + args[0] + " is invalid or is in the wrong format.");
        }
        controlLoop();  //calls control loop for user input

    }
    // adds the edges between actors in a bidirectional fashion.
    public static void addEdge(String actor1, String actor2) {
        LinkedList<String> l;
        if(!graph.containsKey(actor1)){
            l = new LinkedList<>();
        }

        else{
            l = graph.get(actor1);
        }
        l.add(actor2);
        graph.put(actor1,l);
    }
    // Preforms a BFS since edge weights don't exist(as such, they're all equal), saves where we're going into path LL to print back out later when we find matches
    public static LinkedList<String> breadthFirstSearchWithPath(String actor1, String actor2) {

        LinkedList<String> path = new LinkedList<>();
        LinkedList<String> visited = new LinkedList<>();

        for (String name : graph.keySet()) { 
            if (name.equals(actor1)) {
                path.add(name);
                visited.add(name);
            }
        }

        path.add(actor1);
        while (!path.isEmpty()) {       
            String vertex = path.remove();
            visited.add(vertex);
            if (graph.get(vertex).contains(actor2)) {
                for (String actors : graph.keySet()) {
                    if (actors.equals(actor2)) {
                        path.add(actors);
                        visited.add(actors);
                        return path;
                    }
                }
            }
            LinkedList<String> adjacent = new LinkedList<>(new LinkedList<String>(graph.get(vertex)));
            for (int i = 0; i < adjacent.size(); i++) {
                path.add(vertex);
                path.add(adjacent.get(i));
                if (!visited.contains(adjacent.get(i))) {
                    path.add(adjacent.get(i));
                    visited.add(adjacent.get(i));
                }
            }
        }

        return null;

    }
    //This is control loop that takes user input and make sures there is an actor that matches, or just keeps asking for input until we get two good actors
    static void controlLoop() {
        int loopControl1 = 0;
        int loopControl2 = 0;
        String actor1 = "";
        String actor2 = "";
        Scanner scan = new Scanner(System.in);

        while (loopControl1 == 0) {
            System.out.print("Actor 1 name: ");
            actor1 = scan.nextLine().toLowerCase(); //input to lowercase
            if (!graph.containsKey(actor1)) {
                System.out.println("No such actor.");
            } else {
                loopControl1++;
            }
        }
        while (loopControl2 == 0) {

            System.out.print("Actor 2 name: ");
            actor2 = scan.nextLine().toLowerCase(); //input to lowercase

            if (!graph.containsKey(actor2)) {
                System.out.println("No such actor");
            } else {
                loopControl2++;
            }
        }
            try {findShortestPaths(actor1, actor2);}  //try, catch here because my computer ran out of memory a few times calling this funciton
            catch(Exception e) {
                System.out.println("No such path or your computer is out of memory");
            }
        scan.close();
    }

    // this function takes paths and then iterates through it printing shortest path to -> from
    static void findShortestPaths(String actor1, String actor2){

        LinkedList<String> find = breadthFirstSearchWithPath(actor1, actor2);

        if(find == null){
            System.out.println("No path exists.");
        }
        String actors = "";
        for(int i = 0; i < find.size(); i++){
            if (find.get(i).equals(actor1)){
                break;
            }
            actors += find.get(i) + "--> ";
        }
        System.out.println("Path between " + actor1 + " and " + actor2 + ":");
        System.out.print(actor1 + "--> " + actors + actor2);
    }

}
