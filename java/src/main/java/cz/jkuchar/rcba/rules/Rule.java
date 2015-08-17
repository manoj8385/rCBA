package cz.jkuchar.rcba.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import cz.jkuchar.rcba.pruning.Tuple;

@Component
@Scope("prototype")
public class Rule implements Comparable<Rule> {

	// textual representation of rule
	private String text;
	// confidence + support
	private double confidence;
	private double support;

	private double ruleError;
	private double defaultError;
	private Rule defaultRule;

	private boolean marked = false;
	private List<Tuple> replaces = new ArrayList<Tuple>();
	private Map<String, Integer> classCasesCovered = new HashMap<String, Integer>();

	private Map<String, Set<String>> antecendent = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> consequent = new HashMap<String, Set<String>>();

	private Rule() {
		super();
	}

	public String getText() {
		return new String(text);
	}

	public double getConfidence() {
		return confidence;
	}

	public double getSupport() {
		return support;
	}

	public Map<String, Set<String>> getAnt() {
		return antecendent;
	}

	public Map<String, Set<String>> getCons() {
		return consequent;
	}

	public static Rule buildRule(String text, Map<String, Set<String>> meta,
			double confidence, double support) {
		Rule out = new Rule();
		out.text = text;
		out.confidence = confidence;
		out.support = support;
		out.parse(meta);
		return out;
	}

	public static Rule buildRule(String text, double confidence, double support) {
		return buildRule(text, null, confidence, support);
	}

	private void parse(Map<String, Set<String>> meta) {
		if (text != null && text.length() > 0
				&& text.matches("\\{(.*?)\\}\\s*=>\\s*\\{(.+?)\\}")) {

			String[] parts = text.trim().split("\\s*=>\\s*");
			if (parts.length != 2) {
				throw new BadRuleFormatException("Wrong formattting of: "
						+ text);
			}
			if (meta != null) {
				antecendent = parsePart(parts[0], meta);
				consequent = parsePart(parts[1], meta);
			} else {
				antecendent = parsePart(parts[0]);
				consequent = parsePart(parts[1]);
			}
		} else {
			throw new BadRuleFormatException("Wrong formattting of: " + text);
		}
	}

	private Map<String, Set<String>> parsePart(String part,
			Map<String, Set<String>> meta) {
		// remove {}
		part = part.substring(1, part.length() - 1);
		Map<String, Set<String>> out = new HashMap<String, Set<String>>();
		
		for (int i = 0; i < meta.keySet().size(); i++) {
			for (String key : meta.keySet()) {
				if (part.startsWith(key + "=")) {
					for (String val : meta.get(key)) {
						if (part.startsWith(key + "=" + val + ",")
								|| part.equals(key + "=" + val)) {
							out.put(key,
									new HashSet<String>(Arrays.asList(val)));
							int subIndex = (key + "=" + val + ",").length();
							if(subIndex<part.length()){
								part = part.substring(subIndex);
							} else {
								part = "";
							}
							break;
						}
					}

				}
			}
		}
		return out;
	}

	private Map<String, Set<String>> parsePart(String part) {
		// remove {}
		part = part.substring(1, part.length() - 1);
		Map<String, Set<String>> out = new HashMap<String, Set<String>>();
		String[] attrs = part.split(",");
		for (String attr : attrs) {
			Pattern pattern = Pattern.compile("(.*)=(.*)");
			Matcher matcher = pattern.matcher(attr);
			if (matcher.matches()) {
				Set<String> p = new HashSet<String>();
				for (String pp : matcher.group(2).split(",")) {
					p.add(pp);
				}
				out.put(matcher.group(1), p);
			} else if (attr != null && attr.length() > 0) {
				throw new BadRuleFormatException(
						"Wrong formattting of rule items " + attr);
			}
		}
		return out;
	}

	public double getRuleError() {
		return ruleError;
	}

	public void setRuleError(double ruleError) {
		this.ruleError = ruleError;
	}

	public double getDefaultError() {
		return defaultError;
	}

	public void setDefaultError(double defaultError) {
		this.defaultError = defaultError;
	}

	public Rule getDefaultRule() {
		return defaultRule;
	}

	public void setDefaultRule(Rule defaultRule) {
		this.defaultRule = defaultRule;
	}

	public void incClassCasesCovered(String className) {
		if (!this.classCasesCovered.containsKey(className)) {
			classCasesCovered.put(className, 0);
		}
		classCasesCovered.put(className, classCasesCovered.get(className) + 1);
	}

	public void decClassCasesCovered(String className) {
		if (!this.classCasesCovered.containsKey(className)) {
			classCasesCovered.put(className, 0);
		}
		classCasesCovered.put(className, classCasesCovered.get(className) - 1);
	}

	public Map<String, Integer> getClassCasesCovered() {
		return this.classCasesCovered;
	}

	public void addReplace(Tuple tuple) {
		if (!this.replaces.contains(tuple)) {
			replaces.add(tuple);
		}
	}

	public List<Tuple> getReplaces() {
		return this.replaces;
	}

	public boolean isMarked() {
		return this.marked;
	}

	public void mark() {
		this.marked = true;
	}

	public void unmark() {
		this.marked = false;
	}	

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public void setSupport(double support) {
		this.support = support;
	}

	@Override
	public String toString() {
		return "" + text + "\n---support=" + support + "\n---confidence="
				+ confidence + "\n---ant=" + antecendent + "\n---cons="
				+ consequent + "\n---defaultError=" + defaultError
				+ "\n---ruleError=" + ruleError + "\n---defaultRule={"
				+ defaultRule + "}" + "\n";
	}

	public int compareTo(Rule o) {
		int result = Double.compare(o.confidence, this.confidence);
		if (result != 0) {
			return result;
		}
		result = Double.compare(o.support, this.support);
		if (result != 0) {
			return result;
		}
		result = this.antecendent.size() - o.antecendent.size();
		return result;
	}

}
