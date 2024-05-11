import groovy.json.JsonSlurper

def warts2txt() {
    def wartsFile = System.console().readLine("warts file path: ")
    def txtFile = System.console().readLine("txt file path: ")
    def command = ["sc_analysis_dump", wartsFile]
    def process = command.execute()

    def output = process.text
    new File(txtFile).text = output

    println("$wartsFile content succesfully dumped in $txtFile")
}

def warts2json() {
    def wartsFile = System.console().readLine("warts file path: ")
    def jsonFile = System.console().readLine("json file path: ")
    def command = ["sc_warts2json", wartsFile]
    def process = command.execute()

    def output = process.json
    new File(jsonFile).text = output
}

class record {
    char key
    String source
    String dest
    int listID
    int cycleID
    long timestamp
    char destReplied
    double destRTT
    int requestTTL
    int replyTTL
    char haltReason
    int haltReasonData
    char pathComplete

    class HopData {
        String ipAddr
        double rtt
        int tries

        HopData(String ipAddr, Double rtt, Integer tries) {
            this.ipAddr = ipAddr
            this.rtt = rtt
            this.tries = tries
        }

        String toString() {
            return "HopData:\nIP: $ipAddr, RTT: $rtt, tries: $tries\n"
        }
    }

    List<HopData> perHopData = new ArrayList<HopData>()

    String toString() {
            return "Record:\nkey: $key, source: $source, dest: $dest, listID: $listID, cycleID: $cycleID, timestamp: $timestamp, " +
            "destReplied: $destReplied, destRTT: $destRTT, requestTTL: $requestTTL, replyTTL: $replyTTL, haltReason: $haltReason, " +
            "altReasonData: $haltReasonData, pathComplete: $pathComplete, perHopData: $perHopData)\n\n"
    }
}

def records = new ArrayList<record>()

def request(String path, Integer size, List<record> records) {
    
    println("Parsing file: $path")
    file = new File(path)
    def lines = file.readLines()

    if (size == null)
        size = lines.size()
    println("Records to parse: $size")

    def plines = lines.findAll { line ->
        !line.startsWith('#')
    }.take(size) // n. records

    plines.each { line ->
        def tokens = line.tokenize()        
        def r = new record()
        r.key = tokens[0][0]
        r.source = tokens[1]
        r.dest = tokens[2]
        r.listID = tokens[3].toInteger()
        r.cycleID = tokens[4].toInteger()
        r.timestamp = tokens[5].toLong()
        r.destReplied = tokens[6][0]
        r.destRTT = tokens[7].toDouble()
        r.requestTTL = tokens[8].toInteger()
        r.replyTTL = tokens[9].toInteger()
        r.haltReason = tokens[10][0]
        r.haltReasonData = tokens[11].toInteger()
        r.pathComplete = tokens[12][0]
        if (tokens.size() >= 13) {
            for(i = 13; i < tokens.size(); i++) {
                def hopDataToken = tokens[i]
                if (hopDataToken == 'q') {
                    // Se il campo è "q", significa che non c'è stata risposta dall'hop
                    def hopData = new record().new HopData('Unknown', 0.0, 0)
                    r.perHopData.add(hopData)
                } else {
                    // Dividi i dati degli hop in base ai punti e virgola
                    def hopResponses = hopDataToken.split(';')
                    hopResponses.each { hopResponse ->
                        // Dividi ogni risposta degli hop in indirizzo IP, RTT e tentativi
                        def hDF = hopResponse.split(',')
                        if (hDF.size() == 3) {
                            String ipAddr = hDF[0]
                            double rtt = hDF[1].toDouble()
                            int tries = hDF[2].toInteger()
                            def hopData = new record().new HopData(ipAddr, rtt, tries)
                            r.perHopData.add(hopData)
                        } else {
                            // Gestione degli altri casi anomali o errori di formato
                            println("Bad token after hopData parse: $hopResponse")
                        }
                    }
                }
            }
        }
        records.add(r)
    }
}

def avgRTT(ArrayList<record> set) {
    def avg = 0.0
    def ssize = 0
    set.each { r ->
        if (r.destRTT > 0) {
            avg += r.destRTT
            ssize++
        }
    }
    avg /= ssize
    println("Average RTT: $avg, records with non-zero RTT: $ssize")
}


def completedPath(ArrayList<record> set) {
    def count = 0
    set.each { record ->
        if (record.pathComplete == 'C') {
            count++
        }
    }
    def rate = count / set.size() * 100
    println("Path completion rate: ${rate}%, matching records: $count, total records: ${set.size()}")
}

def outcomeRate(ArrayList<record> set, String type) {
    def count = 0
    set.each { record ->
        if (record.haltReason == type) {
            count++
        }
    }
    def rate = count / set.size() * 100
    println("Outcome rate for $type: ${rate}%, matching records: $count, total records: ${set.size()}")
}

//warts2txt()
request("IPv4/raccolta2.txt", 10, records)
outcomeRate(records, 'S')
completedPath(records)
avgRTT(records)
