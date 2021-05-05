package com.vdm.edm.kafka.producers;//import util.properties packages

import org.apache.commons.cli.*;
import com.vdm.edm.protos.SystemLog;
import com.vdm.edm.protos.LogLine;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParsePosition;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemLogLineProtobufProducer {

    static Logger logger = LoggerFactory.getLogger(SystemLogLineProtobufProducer.class);



    static private Properties createKafkaProperties(){
        // create instance for properties to access producer configs
        Properties props = new Properties();
        //Assign localhost id
        props.put("bootstrap.servers", "localhost:9092");
        //Set acknowledgements for producer requests.
        props.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        props.put("retries", 0);
        //Specify buffer size in config
        props.put("batch.size", 16384);
        //Reduce the no of requests less than 0
        props.put("linger.ms", 1);
        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        return props;
    }


    private static LogLine parseLogLine(String line){
    //  Expected format
    //  May  3 06:45:01 ip-10-40-70-208 CRON[26216]: (root) CMD (/usr/local/bin/ntpsync >/usr/local/bin/sync_RESULTS 2>&1)
        String timestamp = "";
        String hostname = "";
        String application = "";
        String processId = "";
        String message = "";
        String[] arrayOfLine = line.split(" ");
        // Timestamp
        LocalDate localDate = LocalDate.now( ZoneOffset.UTC );
        String year = String.valueOf(localDate.getYear());
        String tempTimeStamp = year + " " + arrayOfLine[0] + " " + arrayOfLine[2]  + " " + arrayOfLine[3];
        DateTimeFormatter inFormatter = DateTimeFormatter.ofPattern("yyyy MMM d HH:mm:ss", Locale.ENGLISH);
        TemporalAccessor parsedLogDateTime = inFormatter.parse(tempTimeStamp);
        LocalDateTime datetime = LocalDateTime.from(parsedLogDateTime);
        //ISO_8601
        DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        timestamp = datetime.format(outFormatter);
        hostname = arrayOfLine[4];
        String[] appSplit = arrayOfLine[5].split("\\[");
        application = appSplit[0];
        processId = appSplit[1].substring(0, appSplit[1].indexOf("]"));
        String[] tail = line.split("\\]:");
        message = tail[1].trim();
        LogLine logLine = LogLine.newBuilder()
                .setTimestamp(timestamp)
                .setHostname(hostname)
                .setApplication(application)
                .setProcessId(processId)
                .setMessage(message)
                .build();
        return logLine;
    }


    public static void main(String[] args) throws Exception{
        String topicName = "quickstart-events-topic";

        // create the Options
        Options options = new Options();
        options.addRequiredOption( "s", "syslog", true, "Logfile to read in." );
        // create the parser
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            // parse the command line arguments
            cmd = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java -jar syslog-producer", options );
            System.exit(1);
        }
        // get c option value
        String sysLogFilePath = cmd.getOptionValue("s");
        Properties props =  createKafkaProperties();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(sysLogFilePath));

        }catch (FileNotFoundException e) {
            System.err.println(String.format("File not found %s", e.getMessage()));
            System.exit(1);
        }
        SystemLog.Builder systemLog = SystemLog.newBuilder();
        //NOT IDEAL, only for demo are we loading up a file first then sending it over
        try {
            String fileLogLine = reader.readLine();
            while (fileLogLine != null) {
                LogLine logLine = parseLogLine(fileLogLine);
                systemLog.addLine(logLine);
                System.out.println(fileLogLine);
                // read next line
                fileLogLine = reader.readLine();
            }
        }catch (IOException e) {
            System.err.println("Failed to read syslog file properly.");
        } finally {
            reader.close();
        }
        //NOT IDEAL, only for demo are we loading up a file first then sending it over
        Producer<String, byte[]> producer = new KafkaProducer<String, byte[]>(props);
        for (LogLine line : systemLog.getLineList()) {
            String uuid_key = UUID.randomUUID().toString();
            producer.send(new ProducerRecord<String, byte[]>(topicName, uuid_key, line.toBuilder().build().toByteArray()));
        }
        System.out.println("Message sent successfully");
        producer.close();


        logger.info("Main closed.");
    }
}