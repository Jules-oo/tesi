package unipi.dsl

// team -> year -> cycle -> country + data

class request {

    String reqType // request type
    String filter // filter
    String srcc // source country
    Integer team // team
    String date // year



    def obtain(String t) {
        reqType = t
    }

    def filter(String f) {
        filter = f
    }

    def source_country(String c) {
        srcc = c
    }

    def team(String t) {
        // 1, 2 or 3
        team = t.toInteger()
    }

    def date(String t) {
        // different format for date from yyyy-mm-dd, yyyy-mm, yyyy won't throw an exception but will be ignored
        // and the last chronological dataset available will be used
        // yyyy-mm-dd -> yyyyMMdd is the recommended format for the CAIDA dataset
        date = t.replaceAll("-", "")
        if (date.size() != 8 && date.size() != 6 && date.size() != 4)
            date = null
    }


    def execute() {



    }


}