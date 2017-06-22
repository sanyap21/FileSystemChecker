import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystemCheckerWriter {

	private File[] allFiles;
	//Constructor
	public FileSystemCheckerWriter(String filePath) throws FileNotFoundException{
		File file= new File(filePath);
		this.allFiles = file.listFiles();
		//Sorting the blocks/files in order stored in allFiles array.
		Arrays.sort(this.allFiles, new Comparator<File>() {
			   @Override
			   public int compare(File f1, File f2) {
			    int file1 = Integer.parseInt(f1.getName().substring(9));
			    int file2 = Integer.parseInt(f2.getName().substring(9));
			    return file1 - file2;
			   }
			  });
	}
	//ReadFile
	private String readFile(int blockNum) throws IOException{
		String line= null;
		BufferedReader inputStream= new BufferedReader(new FileReader(allFiles[blockNum]));
		while((line = inputStream.readLine()) != null) {
			
            return line;
           
		}
		inputStream.close();
		return null;
	}
	//Write to file(Map<String, Integer>)
	private void writeToFile(int blockNum, Map<String, Integer> writeMap) throws IOException{
		BufferedWriter outputStream= new BufferedWriter(new FileWriter(allFiles[blockNum]));
		String line= convertMapToString(writeMap);
		outputStream.write(line);
		outputStream.close();
	}
	//Convert Map to String(Map<String, Integer>)
	private String convertMapToString(Map<String, Integer> writeMap) {
		String line=writeMap.toString();
		line= line.replaceAll("=", ":");
		return line;
	}
	//Write to file- Directory(Map<String, Object>)
	private void writeToFileDirectory(int blockNum, Map<String, Object> writeMap) throws IOException{
		BufferedWriter outputStream= new BufferedWriter(new FileWriter(allFiles[blockNum]));
		String line= convertObjectMapToString(writeMap);
		System.out.println("File directory" + line);
		outputStream.write(line);
		outputStream.close();
	}
	//Convert Map to String(Map<String, Object>)
	private String convertObjectMapToString(Map<String, Object> writeMap) {
		String line=writeMap.toString();
		line= line.replaceAll("=", ":");
		return line;
		
	}
	//readFile and create a map out of it
	private Map<String, Integer> readFileAndConvertToMap(int blockNum)throws IOException{
		 
		Map<String, Integer> blockMap= new HashMap<String,Integer>();
	    String line= null;
		BufferedReader inputStream= new BufferedReader(new FileReader(allFiles[blockNum]));
		while((line = inputStream.readLine()) != null) {
            blockMap=createFileMap(line);
		}
		inputStream.close();
		return blockMap;
		
	}
	//Create File Map
	private Map<String, Integer> createFileMap(String line){
		line = line.toLowerCase();
		Map<String, Integer> blockMap= new HashMap<String,Integer>();
		line = line.replaceAll("[\\s{}]","");
		String[] pairs= line.split(",");
		for(int i=0;i<pairs.length;i++){
			String pair = pairs[i];
			String[] keyValue= pair.split(":");
			blockMap.put(keyValue[0], Integer.valueOf(keyValue[1]));
			
		}
		
		return blockMap;
	}
	//Create Directory map which has file inode
	private Map<String, Object> readFileAndCreateDirectoryMap(int blockNum) throws IOException{
		Map<String,Object> directMap= new HashMap<String,Object>();
	    String line= null;
		BufferedReader inputStream= new BufferedReader(new FileReader(allFiles[blockNum]));
		while((line = inputStream.readLine()) != null) {
				line = line.substring(1, line.length()-1);
				int a= line.indexOf("{");
				int b= line.indexOf("}");
				
				String str=line.substring(a,b);
				directMap.put("filename_to_inode_dict", str);
				line = line.replaceAll("[\\s]","");
				int c= line.indexOf("filename_to_inode_dict");
				String str1= line.substring(0,c-1);
				String[] pairs= str1.split(",");
				for(int i=0;i<pairs.length;i++){
					String pair = pairs[i];
					String[] keyValue= pair.split(":");
					directMap.put(keyValue[0], Integer.valueOf(keyValue[1]));
				}
			}
		    inputStream.close();
			return directMap;
	}
	//Check the super block
	private void superBlockCheck() throws IOException{
		Map<String, Integer> superMap = readFileAndConvertToMap(0);
		int rootBlock;
		int freeStart;
		int freeEnd;
		int maxBlocks;
		//Check for device ID
		if(superMap.get("devid")==20){
			System.out.println("1)Device ID is correct for super block " + "fusedata.0 which is "+superMap.get("devid") );
		}
		else
			System.out.println("1)Device ID does not match");
		
		//Check time
		System.out.println("2)Checking if time is in future or past for super block:");
		checkAllTimesInBlock(superMap);
		rootBlock= superMap.get("root");
		//Check root
		directoryCheck(rootBlock, rootBlock);
		freeStart=superMap.get("freestart");
		freeEnd= superMap.get("freeend");
		maxBlocks= superMap.get("maxblocks");
		//Check free list
		System.out.println("3)Free list check result: ");
		freeListCheck(freeStart, freeEnd,maxBlocks);
		writeToFile(0,superMap);
		
		
	}
	//function to check time
	private void checkAllTimesInBlock(Map<String, Integer> map){
		for(String s: map.keySet()){
			if(s.contains("time")){
				long currentTime;
				currentTime=System.currentTimeMillis();
				currentTime=currentTime/1000;
				if(map.get(s)<currentTime)
					System.out.println("Correct time " + s + ": "+ map.get(s));
				else{
					System.out.println("Time in future "+ s + ": "+ map.get(s));
					map.put(s,(int) currentTime);
				}
			}	
		}
	}
	private void checkAllTimesInDirectory(Map<String,Object> map){
		for(String s: map.keySet()){
			if(s.contains("time")){
				long currentTime;
				currentTime=System.currentTimeMillis();
				currentTime=currentTime/1000;
				if((Integer)map.get(s)<currentTime)
					System.out.println("Correct time "+ s + ": " + map.get(s));
				else{
					System.out.println("Time in future "+ s + ": "+ map.get(s));
					map.put(s,(int) currentTime);
				}
			}	
		}
	}
	//function to check the free block list
	private void freeListCheck(int freeStart, int freeEnd, int maxBlocks) throws IOException{
		String line=null;
		String[] lines = new String[freeEnd-freeStart+1];
		for(int i=freeStart, j=0;i<=freeEnd; i++,j++){
		    	
		    	BufferedReader inputStream= new BufferedReader(new FileReader(allFiles[i]));
				while((line = inputStream.readLine()) != null) {
					
	                lines[j]=line;
	                
				}
				inputStream.close();
			 }
            //Task 3a
           List<Integer> blockNum= new ArrayList<Integer>();
           for(int i=0; i<lines.length; i++){
        	   
 				 int blockNo= 0;
 				 lines[i] = lines[i].replaceAll("[\\s{}]","");
 				 String[] blockNames=lines[i].split(",");
 			 for(int j=0; j<blockNames.length;j++){
 				    blockNo=Integer.parseInt(blockNames[j]);
 				    blockNum.add(blockNo);
 				}
 			}
           
           blockNum.sort(new Comparator<Integer>() {
			   @Override
			   public int compare(Integer s1, Integer s2) {
			    return s1-s2;
			   }
			  });
           //Task 3b
           if(blockNum.get(0) < allFiles.length) {
        	   for(int i = blockNum.get(0); i<allFiles.length; i++) {
        		   File f = new File("C:/Users/SANYA/Desktop/Books/FS/fusedata." + i);
        		   if(f.getTotalSpace() > 0) {
        			   System.out.println("Free blocks should be empty. Block + " + "fusedata." + i);
        		   }
        	   }
           }

           Map<Integer, Integer> missingBlocks = new HashMap<Integer, Integer>();
           int fileIndex = freeStart;
           for(int k= 0; k<blockNum.size();k++ ){
				for(int j=allFiles.length;j<maxBlocks;j++){
					//Finding which free block is in which file
					if(j%400 == 0) {
		        		   fileIndex++;
		        	   }
					if(blockNum.get(k)==j)
						k++;
					else {
						System.out.println("Invalid from " + j);
						missingBlocks.put(j, fileIndex);
						BufferedReader inputStream= new BufferedReader(new FileReader(allFiles[fileIndex]));
						String completeLine = "";
						while((line = inputStream.readLine()) != null) {
							completeLine+=line;
						}
						completeLine += ", " + j;
						inputStream.close();
						//Writing the missing blocks  to free block list
						BufferedWriter outputStream= new BufferedWriter(new FileWriter(allFiles[fileIndex]));
						outputStream.write(completeLine);
						outputStream.close();
					 }
					}
				}	
 		}
	// Function to check directories
	private void directoryCheck(int blockNumber, int parentBlockNum) throws IOException{
		Map<String, Object> directoryMap = readFileAndCreateDirectoryMap(blockNumber);
		System.out.println("Time checking for file fusedata."+blockNumber );
		
		checkAllTimesInDirectory(directoryMap);
		
		
		if(directoryMap.containsKey("filename_to_inode_dict")){
			//Splitting filename_to_inode_dict 
			List<String> identifiers = new ArrayList<String>();
			List<String> files= new ArrayList<String>();
			List<Integer> blockNumbers= new ArrayList<Integer>();
            String str= (String)directoryMap.get("filename_to_inode_dict");
            str = str.replaceAll("[\\s{}]","");
    		   String[] pairs= str.split(",");
    		   //check if . and .. exists
    		   if(str.contains(":.:")&& str.contains(":..:")){
    			   System.out.println("File fusedata."+blockNumber+": Contains . and ..");
    		   }
    		   int j = 0;
    		   for(String s: pairs){
    			   String[] pair1= s.split(":");
    			   identifiers.add(pair1[0]);
				   files.add(pair1[1]);
				   blockNumbers.add(Integer.parseInt(pair1[2]));
				   j++;
    		   }
    		   //Checking . and .. block numbers.
    		   for(int i=0;i<files.size();i++){
    			   if( files.get(i).equals(".")){
    				   if(blockNumber==blockNumbers.get(i))
    					   System.out.println("Correct block no. " + blockNumbers.get(i)+ " for file "+ files.get(i));
    				   else{
    					   System.out.println("Wrong block no. "+ blockNumbers.get(i)+ " for file "+ files.get(i));
    					   blockNumbers.set(i,blockNumber);
    				   }
    				       
    			   }
    			   else if(files.get(i).equals("..")){
    				   if(parentBlockNum==blockNumbers.get(i))
    					   
    					   System.out.println("Correct block no. " + blockNumbers.get(i)+ " for file "+ files.get(i));
        			   else{
        				   System.out.println("Wrong block no. "+ blockNumbers.get(i)+ " for file "+ files.get(i));
        				   blockNumbers.set(i, parentBlockNum);
        			      }
    				   }
    			   
    			   }
    		   //checking link count
    		   int linkcount=(Integer)directoryMap.get("linkcount");
    		   if(linkcount==identifiers.size()){
    			   System.out.println("Correct link count" + linkcount +" for File fusedata."+blockNumber );
    		   }
    		   else{
    			   System.out.println("Wrong link count" + linkcount+" for File fusedata."+blockNumber );
    			   linkcount=identifiers.size();
    		   }
    		   
		
    		   for(int i=0;i<identifiers.size();i++){
    			  if( identifiers.get(i).equals("d") && !files.get(i).equals(".") && !files.get(i).equals("..")) {
    				  System.out.println("Checking for directory fusedata."+ blockNumbers.get(i));
    				  
    			  	  directoryCheck(blockNumbers.get(i),blockNumber);  
    			  }
    			  
    			  if(identifiers.get(i).equals("f")){
    				  System.out.println("Checking for file fusedata."+ blockNumbers.get(i));
    				  fileCheck(blockNumbers.get(i),blockNumber);
    				  
    			  }
    		   }
    		   //Combining all attributes of file inode
    		   List<String> fileInodeStr= new ArrayList();
    		   String fileInodeString = "{";
    		   for(int i=0; i< identifiers.size();i++){
    			    fileInodeString += (identifiers.get(i) + ":" + files.get(i)+ ":" + blockNumbers.get(i));
    			    fileInodeString += ", ";
    			    
    			  }
    		   fileInodeString = fileInodeString.substring(0, fileInodeString.length()-2);
    		   fileInodeString += "}";
    		   directoryMap.put("filename_to_inode_dict", fileInodeString);
    		   writeToFileDirectory(blockNumber, directoryMap);
    		  
    		   
		}
		
	}
	private void fileCheck(int blockNumber,int parentBlockNum) throws IOException{
		Map<String, Integer> fileMap = readFileAndConvertToMap(blockNumber);
		System.out.println("Time checking for file fusedata."+blockNumber);
		checkAllTimesInBlock(fileMap);
		boolean arrayFound=true;
		String str=readFile(fileMap.get("location"));
		String[] str1= str.split(",");
		try{
			for(int i=0;i<str1.length;i++){
				Integer.parseInt(str1[i]);
			}
		}catch(Exception e){
			System.out.println("Not an Array");
			arrayFound=false;
		}
		if(arrayFound==true){
			if(fileMap.get("indirect")==1){
				
				System.out.println("Correct value of indirect i.e. it contains an array for fusedata."+blockNumber);
			}
		}
		if(fileMap.containsKey("indirect")){
			int indirectNum= fileMap.get("indirect");
			int sizeOfFile= fileMap.get("size");
			if(indirectNum==0){
				if(sizeOfFile>0&& sizeOfFile<4096 ){
					System.out.println("Valid  size for fusedata."+ blockNumber);
				}
				else
					System.out.println("Invalid size for fusedata."+ blockNumber);
			}
			else if(indirectNum!=0){
				 if(sizeOfFile<(4096*str1.length)){
					 System.out.println("Valid  size for fusedata."+ blockNumber);
				}
				else if(sizeOfFile>(4096*str1.length-1)){
					System.out.println("Valid  size for fusedata."+ blockNumber);
				}
				else
					System.out.println("Invalid size for fusedata."+ blockNumber);
			}
		}
		 writeToFile(blockNumber, fileMap);
	}
	//public function accessible by main to check all
	public void checkFileSystem() throws IOException {
		superBlockCheck();
	}

	public static void main(String[] args) {
		
        try {
			FileSystemCheckerWriter obj= new FileSystemCheckerWriter("C:/Users/SANYA/Desktop/Books/FS");
			obj.checkFileSystem();;
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		
	}

}
