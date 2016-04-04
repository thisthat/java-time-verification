package XAL.items;

/**
 * Created by Giovanni Liva on 11.02.16.
 */
public class XALMetrics {
    //Stub metric taken from infocamere_It_prot.xal
    public static final String stub_metric_name = "mCHECK";
    public static final String stub_metric = "<Metric Id=\"mCHECK\" Type=\"automaton\">\n" +
            "        <System Name=\"checkStatus\" Path=\"infocamere_lt_prot.xal\"/>\n" +
            "        <Input>\n" +
            "          <Parameter LocalVariable=\"IN_device\" TargetVariable=\"IN_device\"/>\n" +
            "          <Parameter LocalVariable=\"IN_instance\" TargetVariable=\"IN_instance\"/>\n" +
            "          <Parameter LocalVariable=\"IN_sub_instance\" TargetVariable=\"IN_sub_instance\"/>\n" +
            "          <Parameter LocalVariable=\"LOC_lastUpdate\" TargetVariable=\"LOC_lastUpdate\"/>\n" +
            "          <Parameter LocalVariable=\"OUT_symbol\" TargetVariable=\"OUT_symbol\"/>\n" +
            "        </Input>\n" +
            "        <Output MetricSymbol=\"OUT_symbol\">\n" +
            "          <Parameter LocalVariable=\"LOC_lastUpdate\" TargetVariable=\"LOC_lastUpdate\"/>\n" +
            "          <Parameter LocalVariable=\"OUT_contexts\" TargetVariable=\"OUT_contexts\"/>\n" +
            "          <Parameter LocalVariable=\"OUT_description\" TargetVariable=\"OUT_description\"/>\n" +
            "          <Parameter LocalVariable=\"OUT_severity\" TargetVariable=\"OUT_severity\"/>\n" +
            "          <Parameter LocalVariable=\"OUT_symbol\" TargetVariable=\"OUT_symbol\"/>\n" +
            "        </Output>\n" +
            "        <Enumeration Type=\"string\">\n" +
            "          <Const Value=\"warning\"/>\n" +
            "          <Const Value=\"passed\"/>\n" +
            "          <Const Value=\"failed\"/>\n" +
            "          <Const Value=\"noUpdate\"/>\n" +
            "        </Enumeration>\n" +
            "      </Metric>\n";
}
