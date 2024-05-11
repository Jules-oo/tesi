import groovy.json.*
import java.text.SimpleDateFormat
import java.util.Date

/*
    The available data are drawn from the Caida Ark IPv4 topology dataset, up to the 2023 cycle measurements.

    The code downloads a file from the server, saves it to a local directory, then decompresses it.
    The object is a WARTS file containing traceroute data from a CAIDA Ark probe.
    The WARTS file content will be subsequently dumped into a JSON format file and parsed.
    Data obtained from the parsing operation will then be processed accordingly to the query's specifications.

    The WARTS file must be processed via scamper software suite, provided from Caida, with one of the following commands:
    sc_analysis_dump <warts_file> > <txt_target_file> -> for the txt dump
    sc_warts2json <warts_file> > <json_target_file> -> for the JSON dump
    sc_warts2txt <warts_file> > <txt_target_file> -> I did not took in account this one as it is not as powerful as the previous ones.

    Currently, the CAIDA submodule includes 2 parser (both for the JSON and txt dump generated files), the HTTP request, and the query.
    The main parser shall be the JSON one, as it provides much more detailed information about the traceroute data. The tradeoff is almost negligible 
    in terms of performance as the JSON parser is somewhat slower compared to the txt one, the latter providing much lighter (and much less detailed) files.

    The query will be executed on the parsed data, and the results will be displayed in a user-friendly format, as it expands the GeoDSL language model for
    Internet Measurements and Analysis.

    Capitani Giulio
*/


// class which helds the measurement data from every hop made by the traceroute packet
class Hop {
    String addr
    int probe_ttl
    int probe_id
    int probe_size
    Tx tx
    double rtt
    int reply_ttl
    int reply_tos
    int reply_size
    int reply_ipid
    int icmp_type // icmp packet type
    int icmp_code // icmp packet code
    // Integer can be assigned as null, int can not
    // Q refers to a packet lost, as for the next fields they represent the icmp packet data that was lost
    Integer icmp_q_ttl // icmp packet ttl
    Integer icmp_q_ipl // icmp ip length 
    Integer icmp_q_tos // icmp type of service

    Hop(String addr, int probe_ttl, int probe_id, int probe_size, int txsec, int txusec, double rtt, int reply_ttl, int reply_tos, int reply_size, int reply_ipid, int icmp_type, int icmp_code, def icmp_q_ttl, def icmp_q_ipl, def icmp_q_tos) {
        this.addr = addr
        this.probe_ttl = probe_ttl
        this.probe_id = probe_id
        this.probe_size = probe_size
        def tx = new Tx(txsec, txusec)
        this.rtt = rtt
        this.reply_ttl = reply_ttl
        this.reply_tos = reply_tos
        this.reply_size = reply_size
        this.reply_ipid = reply_ipid
        this.icmp_type = icmp_type
        this.icmp_code = icmp_code
        this.icmp_q_ttl = icmp_q_ttl 
        this.icmp_q_ipl = icmp_q_ipl 
        this.icmp_q_tos = icmp_q_tos 
    }
}

// class which helds the start time of the traceroute packet
// sec and usec refers (?) to the cycle time
class Start {
    int sec
    int usec
    Date ftime // definire formato data al momento del parsing

    Start(int sec, int usec, Date ftime) {
        this.sec = sec
        this.usec = usec
        this.ftime = ftime
    }
}

// transmitter information regarding the hop
class Tx {
    int sec
    int usec
    Tx(int sec, int usec) {
        this.sec = sec
        this.usec = usec
    }
}


// class which helds the measurement data from the traceroute execution as a whole
class Record {
    String type
    String version
    int userid
    String method
    String src
    String dst
    int icmp_sum
    String stop_reason
    int stop_data
    Start start
    int hop_count
    int attempts
    int hoplimit
    int firsthop
    int wait
    int wait_probe
    int tos
    int probe_size
    int probe_count
    List<Hop> hops = []
}

def records = new ArrayList<Record>()
/*
def foo(String path) {
    def slurper = new groovy.json.JsonSlurper()
    new File(path).eachLine { l ->
        def record = slurper.parseText(l)
        if (record.type == "trace") {
            def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            Date date = dateFormat.parse(record.start.ftime)
            def s = new Start(record.start.sec, record.start.usec, date)
        
            println ("$s.ftime, $record.start.ftime")
            return
        }
    }
}*/


// function that parses the json file and fills the records list
def toParse(String path, int size, List<Record> records) {

    def slurper = new groovy.json.JsonSlurper()
    println("Parsing file: $path, size: $size")
    new File(path).each { line -> // unico modo per parsare tutto il documento in maniera corretta con slurper
        def record = slurper.parseText(line)
        if (record.type == "trace") {
            def r = new Record()
            r.type = record.type
            r.version = record.version
            r.userid = record.userid
            r.method = record.method
            r.src = record.src
            r.dst = record.dst
            r.icmp_sum = record.icmp_sum
            r.stop_reason = record.stop_reason
            r.stop_data = record.stop_data

            def start = record.start
            def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            Date date = dateFormat.parse(start.ftime)
            def s = new Start(start.sec, start.usec, date)
            r.start = s

            r.hop_count = record.hop_count
            r.attempts = record.attempts
            r.hoplimit = record.hoplimit
            r.firsthop = record.firsthop
            r.wait = record.wait
            r.wait_probe = record.wait_probe
            r.tos = record.tos
            r.probe_size = record.probe_size
            r.probe_count = record.probe_count

            def hops = record.hops
            hops.each { hop ->
                def tx = hop.tx                
                //    Hop(String addr, int probe_ttl, int probe_id, int probe_size, int txsec, int txusec, double rtt, int reply_ttl, int reply_tos, int reply_size, int reply_ipid, int icmp_type, int icmp_code, int icmp_q_ttl, int icmp_q_ipl, int icmp_q_tos)
                def h = new Hop(hop.addr, hop.probe_ttl, hop.probe_id, hop.probe_size, tx.sec, tx.usec, hop.rtt, hop.reply_ttl, hop.reply_tos, hop.reply_size, hop.reply_ipid, hop.icmp_type, hop.icmp_code, hop.icmp_q_ttl, hop.icmp_q_ipl, hop.icmp_q_tos)
                r.hops.add(h)
            }

            records.add(r)
        }
    }
    println records.size()
}



toParse("IPv4/raccolta2.json", 100, records)
println(records[0].hops[0].addr)