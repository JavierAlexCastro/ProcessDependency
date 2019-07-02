package code;

import java.io.*; 
import java.util.*;

public class ProcessDependency {
	
	public List<ArrayList<String>> readFile(File fp) {
		boolean isFirstLine = true;
		List<ArrayList<String>> d_list = new ArrayList<ArrayList<String>>(); //{ {nsubj, system-17, system-9}, {}, etc}
		try{
			BufferedReader br = new BufferedReader(new FileReader(fp)); 
			String line; //holds a line of the file being read
			try {
				while ((line = br.readLine()) != null) {
					ArrayList<String> temp_list = new ArrayList<String>(); //arraylist for each line
					if(isFirstLine) { //first line in file is the sentence
						temp_list.add(line);
						isFirstLine = false;
					}else {
						String temp_str = line.replaceAll("\\)", "").replaceAll(",", "\\("); //remove ")", replace "," with "("
						String parts[] = temp_str.split("\\("); //split on "("
						for (int i=0; i<parts.length; i++){
							temp_list.add(parts[i].trim()); //add elements to temp arraylist
						 }
					}
					d_list.add(temp_list); //add temp arraylist to final arraylist
				}
			}catch (IOException e) {
				System.out.println("File empty.");
			}
		}catch(FileNotFoundException e) {
			System.out.println("File was not found.");
		}
		return d_list;
	}

	public List<ArrayList<String>> getRelatedDependencies(List<ArrayList<String>> d_list, String str1, String str2) {
		List<ArrayList<String>> related_list = new ArrayList<ArrayList<String>>();
		for(int i = 0; i<d_list.size(); i++) {
			ArrayList<String> temp_list = new ArrayList<String>(); //arraylist for inner arraylist
			temp_list = d_list.get(i);
			if(temp_list.get(1).toString().equals(str1) || temp_list.get(1).toString().equals(str2) || temp_list.get(2).toString().equals(str1) || temp_list.get(2).toString().equals(str2)) {
				related_list.add(temp_list); //add dependency to related list
				//System.out.println("Added related: "+temp_list.get(0).toString()+"("+temp_list.get(1).toString()+", "+temp_list.get(2).toString()+")");
			}
		}
		return related_list;
	}
	
	public boolean hasTAG(List<ArrayList<String>> s_list, String tag){
		ArrayList<String> temp_list = new ArrayList<String>(); //arraylist for each individual dependency
		boolean hasTag = false;
		for(int i = 0; i<s_list.size(); i++) {
			temp_list = s_list.get(i);
			if(temp_list.get(0).toString().equals(tag)) {
				hasTag = true;
			}
		}
		return hasTag;
		
	}
	
	public void processACL(List<ArrayList<String>> s_list, String sentence, List<ArrayList<String>> subject, List<ArrayList<String>> d_list) {
		ArrayList<String> temp_list = new ArrayList<String>(); //individual dependency
		List<ArrayList<String>> acl_list = new ArrayList<ArrayList<String>>(); //dependencies related to ACL
		ArrayList<String> temp_xcomp = new ArrayList<String>(); //individual dependency
		List<ArrayList<String>> xcomp_list = new ArrayList<ArrayList<String>>(); //dependencies related to xcomp
		for(int j = 0; j<s_list.size(); j++) {
			temp_list = s_list.get(j);
			if(temp_list.get(0).toString().equals("acl")) {
				acl_list = getRelatedDependencies(d_list, temp_list.get(2).toString(), temp_list.get(2).toString());
				for(int i = 0; i<acl_list.size(); i++) {
					temp_xcomp = acl_list.get(i);
					if(temp_xcomp.get(0).toString().equals("xcomp")) {
						xcomp_list = getRelatedDependencies(d_list, temp_xcomp.get(2).toString(), temp_xcomp.get(2).toString());
						printSentence(subject, sentence);
						printSentence(xcomp_list, sentence);
						System.out.println("");
					}
				}
			}
		}
	}
	
	public void printSentence(List<ArrayList<String>> s_list, String str) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<String> ignore = new ArrayList<String>();
		ignore.add("cc");
		ignore.add("conj:or");
		ignore.add("conj:and");
		ignore.add("root");
		ignore.add("acl");
		ignore.add("nsubj");
		
		for(int i = 0; i<s_list.size(); i++) {
			String temp_str;
			int temp_index;
			String elements[];
			String n_dependency = s_list.get(i).get(0).toString();
			
			temp_str = s_list.get(i).get(1).toString(); //first element in dependency
			elements = temp_str.split("-");
			temp_index = Integer.valueOf(elements[1]);
			if(!indexes.contains(temp_index)) { //if index not already added
				if(!ignore.contains(n_dependency)) {
					indexes.add(Integer.valueOf(elements[1]));
				}
			}
			temp_str = s_list.get(i).get(2).toString(); //second element in dependency
			elements = temp_str.split("-");
			temp_index = Integer.valueOf(elements[1]);
			if(!indexes.contains(temp_index)) { //if index not already added
				if(!ignore.contains(n_dependency)) {
					indexes.add(Integer.valueOf(elements[1]));
				}
			}
		}
		Collections.sort(indexes);
		String sentence[] = str.split("[^a-zA-Z']");
		for(int i = 0; i<indexes.size(); i++) {
			System.out.print(sentence[indexes.get(i)-1] + " ");
		}
	}
	
	public int process(List<ArrayList<String>> d_list) {
		String sentence = d_list.get(0).get(0).toString(); //get sentence from dependency arraylist
		d_list.remove(0); //remove sentence from dependency arraylistlist
		ArrayList<String> temp_list = new ArrayList<String>(); //arraylist for each individual dependency
		for(int i = 0; i<d_list.size(); i++) {
			temp_list = d_list.get(i);
			if(temp_list.get(0).toString().equals("nsubj")) {
				List<ArrayList<String>> s_list = new ArrayList<ArrayList<String>>(); //list for a new sentence
				s_list = getRelatedDependencies(d_list, temp_list.get(1).toString(), temp_list.get(2).toString());
				printSentence(s_list, sentence);
				System.out.print("\n"); //space after a sentence
				List<ArrayList<String>> subject = new ArrayList<ArrayList<String>>(); //subject only
				subject = getRelatedDependencies(d_list, temp_list.get(2).toString(), temp_list.get(2).toString());
				//printSentence(subject, sentence);
				//check if ACL
				if(hasTAG(s_list, "acl")) {
					processACL(s_list, sentence, subject, d_list);
					
				}
				//continue processing other tags after printing based on nsubj
			}
		}
		return 0;
	}
	
	public static void main(String[] args) { 
		ProcessDependency pd = new ProcessDependency();
		File fp = new File("resources//dependency1.txt");
		List<ArrayList<String>> d_list = pd.readFile(fp);
		pd.process(d_list);
		
	}
}