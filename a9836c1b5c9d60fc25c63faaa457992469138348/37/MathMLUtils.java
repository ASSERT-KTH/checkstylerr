package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.MathMarkUpType;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class MathMLUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MathMLUtils.class);
  private static final SnuggleEngine SNUGGLE_ENGINE = new SnuggleEngine();

  /**
   * list of false positive identifiers
   */
  public final static Set<String> BLACKLIST = prepareBlacklist();
  private static boolean summarizeSubscripts = false;

  public static String getEngine() {
    return engine;
  }

  public static void setEngine(String engine) {
    MathMLUtils.engine = engine;
  }

  //@TODO: Make this configurable
  private static String engine = "snuggle";

  private static Set<String> prepareBlacklist() {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();

    // operators
    builder.add("sin", "cos", "tan", "min", "max", "argmax", "arg max", "argmin", "arg min", "inf",
        "lim", "log", "lg", "ln", "exp", "sup", "supp", "lim sup", "lim inf", "arg", "dim",
        "dimension", "cosh", "arccos", "arcsin", "arctan", "atan", "arcsec", "rank", "nullity",
        "det", "Det", "ker", "sec", "cot", "csc", "sinh", "coth", "tanh", "arcsinh", "arccosh",
        "arctanh", "atanh", "def", "image", "avg", "average", "mean", "var", "Var", "cov", "Cov",
        "diag", "span", "floor", "ceil", "head", "tail", "tr", "trace", "div", "mod", "round", "sum",
        "Re", "Im", "gcd", "sng", "sign", "length");

    // math symbols
    // http://unicode-table.com/en/blocks/mathematical-operators/
    List<String> mathOperators = listOfStrings('\u2200', 256);
    mathOperators.remove("∇"); // nabla is often an identifier, so we should keep it
    builder.addAll(mathOperators);
    // http://unicode-table.com/en/blocks/supplemental-mathematical-operators/
    builder.addAll(listOfStrings('\u2A00', 256));

    // others math-related
    builder.add(":=", "=", "+", "~", "e", "°", "′", "^");

    // symbols
    // http://unicode-table.com/en/blocks/spacing-modifier-letters/
    builder.addAll(listOfStrings('\u02B0', 80));
    // http://unicode-table.com/en/blocks/miscellaneous-symbols/
    builder.addAll(listOfStrings('\u2600', 256));
    // http://unicode-table.com/en/blocks/geometric-shapes/
    builder.addAll(listOfStrings('\u25A0', 96));
    // http://unicode-table.com/en/blocks/arrows/
    builder.addAll(listOfStrings('\u2190', 112));
    // http://unicode-table.com/en/blocks/miscellaneous-technical/
    builder.addAll(listOfStrings('\u2300', 256));
    // http://unicode-table.com/en/blocks/box-drawing/
    builder.addAll(listOfStrings('\u2500', 128));

    // false identifiers
    builder.add("constant", "const", "true", "false", "new", "even", "odd", "subject", "vs", "versus",
        "iff");
    builder.add("where", "unless", "otherwise", "else", "on", "of", "or", "with", "if", "then", "from",
        "to", "by", "has", "within", "when", "out", "and", "for", "as", "is", "at", "such", "that",
        "before", "after");

    // identifier that are also English (stop-)words
    builder.add("a", "A", "i", "I");

    // punctuation
    builder.add("%", "?", "!", ":", "'", "…", ";", "(", ")", "\"", "′′′′′", "′′′′", "′′′", "′′", "′",
        " ", " ");

    // special chars
    builder.add("_", "|", "*", "#", "{", "}", "[", "]", "$", "&", "/", "\\");

    // units
    builder.add("mol", "dB", "mm", "cm", "km", "Hz");

    return builder.build();
  }

  private static List<String> listOfStrings(char from, int amount) {
    List<String> result = Lists.newArrayListWithCapacity(amount);
    for (char c = from; c < from + amount; c++) {
      result.add(CharUtils.toString(c));
    }
    return result;
  }

  public static Multiset<String> extractIdentifiers(MathTag math, Boolean useTeXIdentifiers, String url) {
    try {
      return tryExtractIdentifiers(math, useTeXIdentifiers, url);
    } catch (Exception e) {
      LOGGER.warn("exception occurred during 'extractIdentifiers'. Returning an empty set", e);
      return HashMultiset.create();
    }
  }

  private static Multiset<String> tryExtractIdentifiers(MathTag math, Boolean useTeXIdentifiers, String url) {
    if (math.getMarkUpType() != MathMarkUpType.MATHML) {
      return extractIdentifiersFromTex(math.getTagContent(), useTeXIdentifiers, url);
    } else {
      return extractIdentifiersFromMathML(math.getContent(), useTeXIdentifiers, false);
    }
  }

  public static Multiset<String> extractIdentifiersFromTex(String tex, boolean useTeX, String url) {
    if (useTeX) {
      try {
        Multiset<String> identifiers = TexInfo.getIdentifiers(tex, url);
        //TODO: Migrate to texvcinfo
        identifiers.removeIf(x -> x.equals("\\infty") || x.startsWith("\\operatorname"));
        if (summarizeSubscripts) {
          for (String identifier : identifiers.elementSet()) {
            if (identifier.matches("(.*?)_\\{[a-zA-Z0-9]\\}$")) {
              identifiers.remove(identifier, Integer.MAX_VALUE);
              identifiers.add(identifier.replaceAll("(.*?)_\\{[a-zA-Z0-9]\\}$", "$1_"));
            }
          }
        }
        return identifiers;

      } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException | TransformerException e) {
        e.printStackTrace();
        return HashMultiset.create();
      }
    }
    String mathML = texToMathML(tex);
    LOGGER.debug("converted {} to {}", tex.replaceAll("\\s+", " "), mathML);
    return extractIdentifiersFromMathML(mathML, false, false);
  }

  public static Multiset<String> extractIdentifiersFromMathML(String mathML, Boolean useTeXIdentifiers, boolean useBlacklist) {
    try {
      return tryParseWithXpath(mathML, useTeXIdentifiers, useBlacklist);
    } catch (Exception e) {
      LOGGER.warn("exception occurred while trying to parse mathML with xpath... "
          + "backing off to the regexp parser.", e);
      return parseWithRegex(mathML);
    }
  }

  private static Multiset<String> tryParseWithXpath(String mathML, boolean useTeX, boolean useBlacklist) {
    XML xml = new XMLDocument(mathML);
    xml = xml.registerNs("m", "http://www.w3.org/1998/Math/MathML");
    Multiset<String> result = HashMultiset.create();

    List<XML> subscript = xml.nodes("//m:msub");
    for (XML msubNode : subscript) {
      List<String> text = msubNode.xpath("*[normalize-space()]/text()");
      if (text.size() != 2) {
        String debugText = text.toString().replaceAll("\\s+", " ");
        String nmsubMathMl = msubNode.toString().replaceAll("\\s+", " ");
        LOGGER.debug("unexpected input: {} for {}", debugText, nmsubMathMl);
        continue;
      }
      String id;
      String sub;
      if (useTeX) {
        id = UnicodeMap.string2TeX(text.get(0));
        sub = "{" + UnicodeMap.string2TeX(text.get(1)) + "}";
      } else {
        id = UnicodeUtils.normalizeString(text.get(0));
        sub = UnicodeUtils.normalizeString(text.get(1));
      }
      if (useBlacklist && BLACKLIST.contains(id)) {
        continue;
      }
      if (isNumeric(id)) {
        continue;
      }
      result.add(id + "_" + sub);
    }

    List<String> allIdentifiers = xml.xpath("//m:mi[not(ancestor::m:msub)]/text()");
    for (String rawId : allIdentifiers) {
      String id;
      if (useTeX) {
        id = UnicodeMap.string2TeX(rawId);
        id = id.replaceAll("^\\{(.*)\\}$", "$1");
      } else {
        id = UnicodeUtils.normalizeString(rawId);
      }
      if (useBlacklist && BLACKLIST.contains(id)) {
        continue;
      }
      if (isNumeric(id)) {
        continue;
      }

      result.add(id);
    }

    return result;
  }

  public static boolean isNumeric(String id) {
    return id.matches("\\d+.?\\d*");
  }

  private static Multiset<String> parseWithRegex(String mathML) {
    Pattern miTag = Pattern.compile("<mi.*?>(.+?)</mi>");
    Matcher matcher = miTag.matcher(mathML);

    Multiset<String> ids = HashMultiset.create();
    while (matcher.find()) {
      String id = matcher.group(1);
      ids.add(id);
    }

    ids.removeAll(BLACKLIST);
    return ids;
  }

  public static String texToMathML(String tex) {

    try {
      if (engine.equals("snuggle")) {
        SnuggleSession session = SNUGGLE_ENGINE.createSession();
        String cleanTexString = cleanTexString(tex);
        session.parseInput(new SnuggleInput("$$ " + cleanTexString + " $$"));
        String xmlString = session.buildXMLString();
        return xmlString;
      } else {
        return TeX2MathML.TeX2MML(tex);
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }


  }

  public static String cleanTexString(String tex) {
    // strip text blocks
    tex = tex.replaceAll("\\\\(text|math(:?bb|bf|cal|frak|it|sf|tt))\\{.*?\\}", "");
    // strip arbitrary operators
    tex = tex.replaceAll("\\\\operatorname\\{.*?\\}", "");
    // strip some unparseble stuff
    tex = tex.replaceAll("\\\\(rang|left|right|rangle|langle)|\\|", "");
    // strip dim/log
    tex = tex.replaceAll("\\\\(dim|log)_(\\w+)", "$1");
    // strip "is element of" definitions
    tex = tex.replaceAll("^(.*?)\\\\in", "$1");
    // strip indices
    tex = tex.replaceAll("^([^\\s\\\\\\{\\}])_[^\\s\\\\\\{\\}]$", "$1");
    return tex;
  }

}
