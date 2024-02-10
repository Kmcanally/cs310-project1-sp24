package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        
        // JSON containers
        JsonObject json = new JsonObject();     // Primary JSON object
        JsonObject scheduletype = new JsonObject();     // JSON object for schedule type
        JsonObject subject = new JsonObject();  // JSON object for subject
        JsonObject course = new JsonObject();   // JSON object for course
        JsonArray section = new JsonArray();    // JSON array for section
        
        // Iterator
        Iterator<String[]> iterator = csv.iterator();
        
        // CSV getting headers for header row
        String[] headers = iterator.next();
        
        // Creating HashMap key-value pairs of header and index
        HashMap<String, Integer> headerRow = new HashMap<>();
        for(int i = 0; i < headers.length; i++) {
            headerRow.put(headers[i], i);
        }
        
        // Iterator loop
        while(iterator.hasNext()) {
            String[] record = iterator.next();
            
            // separate and store subject id and number
            String sIDNum = record[headerRow.get(NUM_COL_HEADER)];
            String[] sNumSplit = sIDNum.split("\\s+");
            String sID = sNumSplit[0];
            String sNum = sNumSplit[1];
            
            // get schedule type data
            String typeAbb = record[headerRow.get(TYPE_COL_HEADER)];
            String typeDes = record[headerRow.get(SCHEDULE_COL_HEADER)];
            
            // get subject data
            /*String sID is done above*/
            String sName = record[headerRow.get(SUBJECT_COL_HEADER)];
            
            // get course data
            /*String sID is done above
            String sNum is done above
            String sIDNum is done above*/
            String cDesc = record[headerRow.get(DESCRIPTION_COL_HEADER)];
            Integer credits = Integer.valueOf(record[headerRow.get(CREDITS_COL_HEADER)]);
            
            // get section data
            /*String sID is done above
            String sNum is done above*/
            Integer crn = Integer.valueOf(record[headerRow.get(CRN_COL_HEADER)]);
            String sect = record[headerRow.get(SECTION_COL_HEADER)];
            String type = record[headerRow.get(TYPE_COL_HEADER)];
            String start = record[headerRow.get(START_COL_HEADER)];
            String end = record[headerRow.get(END_COL_HEADER)];
            String days = record[headerRow.get(DAYS_COL_HEADER)];
            String where = record[headerRow.get(WHERE_COL_HEADER)];
            
                // Using both an array and an ArrayList to separate and format instructors
            String[] instructorArray = record[headerRow.get(INSTRUCTOR_COL_HEADER)].split(",");
            ArrayList<String> instructors = new ArrayList<>();
            
            for (String i : instructorArray) {
                instructors.add(i.trim());
            }
            
            // put schedule data in json object
            scheduletype.put(typeAbb, typeDes);
            
            // put subject data in json object
            subject.put(sID, sName);
            
            // populate a new json object for course data
            JsonObject courseData = new JsonObject();   // create new json object for storing nested data
            courseData.put(SUBJECTID_COL_HEADER, sID);
            courseData.put(NUM_COL_HEADER, sNum);
            courseData.put(DESCRIPTION_COL_HEADER, cDesc);
            courseData.put(CREDITS_COL_HEADER, credits);
            
            // put populated json object in primary course json object
            course.put(sIDNum, courseData);
            
            // populate a new json object for section data
            JsonObject sectionData = new JsonObject();
            sectionData.put(CRN_COL_HEADER, crn);
            sectionData.put(SUBJECTID_COL_HEADER, sID);
            sectionData.put(NUM_COL_HEADER, sNum);
            sectionData.put(SECTION_COL_HEADER, sect);
            sectionData.put(TYPE_COL_HEADER, type);
            sectionData.put(START_COL_HEADER, start);
            sectionData.put(END_COL_HEADER, end);
            sectionData.put(DAYS_COL_HEADER, days);
            sectionData.put(WHERE_COL_HEADER, where);
            sectionData.put(INSTRUCTOR_COL_HEADER, instructors);
            
            
            // add section data to json array
            section.add(sectionData);
        }
        
        // Adding all elements to main json object
        json.put("scheduletype", scheduletype);
        json.put("subject", subject);
        json.put("course", course);
        json.put("section", section);
        
        return Jsoner.serialize(json);
        
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
        // Create csv string
        String csvString;
        
        // Make StringWriter and CSVWriter
        StringWriter sWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(sWriter, '\t', '"', '\\', "\n");
        
        // Define CSV headers then write to CSV
        String[] headers = {CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER, DESCRIPTION_COL_HEADER, SECTION_COL_HEADER, TYPE_COL_HEADER,
        CREDITS_COL_HEADER, START_COL_HEADER, END_COL_HEADER, DAYS_COL_HEADER, WHERE_COL_HEADER, SCHEDULE_COL_HEADER, INSTRUCTOR_COL_HEADER};
        csvWriter.writeNext(headers);
        
        // Create json objects and arrays
        JsonObject scheduletype = (JsonObject) json.get("scheduletype");
        JsonObject subjects = (JsonObject) json.get("subject");
        JsonObject courses = (JsonObject) json.get("course");
        JsonArray sections = (JsonArray) json.get("section");
        
        // Initialize values to add to each row
        String crn, subject, num, description, sID, type, credits, start, end, days, where, schedule, instructor;
        
        // Looping through json data
        for (Object sectObj : sections) {
            JsonObject cSection = (JsonObject) sectObj; // focuses on current json object within loop
            
            // Collect first three values (crn, subject, and num) for current CSV row
            crn = String.valueOf(cSection.get(CRN_COL_HEADER));
            subject = (String) subjects.get((String) cSection.get(SUBJECTID_COL_HEADER));
            num = ((String) cSection.get(SUBJECTID_COL_HEADER)) + " " + ((String) cSection.get(NUM_COL_HEADER));
            
            // Create a new json object for nested json objects within courses
            JsonObject cCourse = (JsonObject) courses.get(num); // references most recent number grabbed to pull specific course
            
            // continue gathering values from json objects
            description = (String) cCourse.get(DESCRIPTION_COL_HEADER);
            sID = (String) cSection.get(SECTION_COL_HEADER);
            type = (String) cSection.get(TYPE_COL_HEADER);
            credits = String.valueOf(cCourse.get(CREDITS_COL_HEADER));
            start = (String) cSection.get(START_COL_HEADER);
            end = (String) cSection.get(END_COL_HEADER);
            days = (String) cSection.get(DAYS_COL_HEADER);
            where = (String) cSection.get(WHERE_COL_HEADER);
            schedule = (String) scheduletype.get(type);
            
            // Populate a List with instructors to join them in one string
            List<String> instructors = (List<String>) cSection.get(INSTRUCTOR_COL_HEADER);
            instructor = String.join(", ", instructors);
            
            // Write current CSV row
            csvWriter.writeNext(new String[]{crn, subject, num, description, sID, type, credits, start, end, days, where, schedule, instructor});
        }
        
        // Write to csvString to return CSV
        csvString = sWriter.toString();
        
        return csvString; 
        
    }
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}