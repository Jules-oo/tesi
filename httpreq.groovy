import java.net.*

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

def teamselect() {
    def url = new URL("https://publicdata.caida.org/datasets/topology/ark/ipv4/probe-data/")
    def conn = url.openConnection()

    def responseCode = conn.getResponseCode()
    if (responseCode == 200) {
        println("Available team data:")
        InputStream is = conn.getInputStream()
        BufferedReader br = new BufferedReader(new InputStreamReader(is))
        String line
        while ((line = br.readLine()) != null) {
            if (line.contains("team-") && line.contains("href")) {
                def startIndex = line.indexOf("team-")
                def endIndex = line.indexOf("/", startIndex + 1)
                def teamName = line.substring(startIndex, endIndex)
                println(teamName)
            }
        }
        br.close()
    }
}

def yearselect(String team) {
    def url = new URL("https://publicdata.caida.org/datasets/topology/ark/ipv4/probe-data/" + team + "/")
    def conn = url.openConnection()

    def responseCode = conn.getResponseCode()
    if (responseCode == 200) {
        println("Available year data per $team:")
        InputStream is = conn.getInputStream()
        BufferedReader br = new BufferedReader(new InputStreamReader(is))
        String line
        while ((line = br.readLine()) != null) {
            if (line.contains("href") && line.contains("20")) {
                def startIndex = line.indexOf("20")
                def endIndex = line.indexOf("/", startIndex + 1)
                def year = line.substring(startIndex, endIndex)
                println(year)
            }
        }
        br.close()
    }
}

def cycleselect(String team, String year) {
    def url = new URL("https://publicdata.caida.org/datasets/topology/ark/ipv4/probe-data/" + team + "/" + year + "/")
    def conn = url.openConnection()

    def responseCode = conn.getResponseCode()
    if (responseCode == 200) {
        println("Available cycle data per $team in $year:")
        InputStream is = conn.getInputStream()
        BufferedReader br = new BufferedReader(new InputStreamReader(is))
        String line
        while ((line = br.readLine()) != null) {
            if (line.contains("href") && line.contains("cycle-")) {
                def startIndex = line.indexOf("cycle-")
                def endIndex = line.indexOf("/", startIndex + 1)
                def cycle = line.substring(startIndex, endIndex)
                println(cycle)
            }
        }
        br.close()
    }
}

def fileselect(String year, String team, String cycle) {
    def url = new URL("https://publicdata.caida.org/datasets/topology/ark/ipv4/probe-data/" + team + "/" + year + "/" + cycle + "/")
    def conn = url.openConnection()

    def responseCode = conn.getResponseCode()
    if (responseCode == 200) {
        println("Available files per $team in $year in $cycle:")
        InputStream is = conn.getInputStream()
        BufferedReader br = new BufferedReader(new InputStreamReader(is))
        String line
        while ((line = br.readLine()) != null) {
            if (line.contains("href") && line.contains("warts")) {
                def startIndex = line.indexOf("<a href=") + "<a href=".length() + 1
                def endIndex = line.indexOf("\"", startIndex + 1)
                def filename = line.substring(startIndex, endIndex)
                println(filename)
            }
        }
        br.close()
    }

}

def httpReq(String team, String year, String cycle, String filename) {

    String surl = "https://publicdata.caida.org/datasets/topology/ark/ipv4/probe-data/"
    surl += team + "/" + year + "/" + cycle + "/" + filename
    def url = new URL(surl)

    def conn = url.openConnection()
    conn.setRequestMethod("GET")

    def responseCode = conn.getResponseCode()
    println "Response Code: ${responseCode}"

    if (responseCode == 200) {
        File dir = new File("raw_data")
        if (!dir.exists()) 
            dir.mkdirs()

        InputStream is = conn.getInputStream()

        File file = new File(dir, "abz2-uk.team-probing.c010445.20230101.warts.gz")
        FileOutputStream fos = new FileOutputStream(file)

        println("Downloading...")
        byte[] buffer = new byte[4096]
        int bytesRead
        while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead)
        }
        fos.close()
        is.close()
        println("Download completed.")
    
        println("Decompressing...")
        Process p = "gunzip raw_data/abz2-uk.team-probing.c010445.20230101.warts.gz".execute()
        p.waitFor()
        println("Decompression completed.")
    } else {
        println("Error during the HTTP request to the server: code ${responseCode}")
    }
}

/*teamselect()
yearselect("team-1")
cycleselect("team-1", "2023")*/
fileselect("2023", "team-1", "cycle-20230131") // browse through files inside the directory of choichet