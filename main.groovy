package unipi.dsl

/*

    Main module of the CAIDA Ark submodule for GeoDSL.
    In this module is described how the user can interact with the CAIDA Ark dataset through query operations.

    No API nor SDK is provided by CAIDA, so the data must be downloaded from the official website, through HTTP requests.

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



