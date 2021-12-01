package eu.excitementproject.eop.alignmentedas.p1eda.visualization;

/**
 * An implementation if the {@link eu.excitementproject.eop.alignmentedas.p1eda.visualization.Visualizer} interface, based on {@link Brat visualizer http://brat.nlplab.org/embed.html}

 * Visualizes POS and dependency relation annotations and alignments.
 * Provides GUI for filtering annotations and alignments. 
 * 
 * @author Eden Erez
 * @since Jan 6, 2015
 *
 */


import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

import eu.excitement.type.alignment.Link;
import eu.excitementproject.eop.alignmentedas.p1eda.TEDecisionWithAlignment;
import eu.excitementproject.eop.alignmentedas.p1eda.subs.FeatureValue;
import eu.excitementproject.eop.alignmentedas.p1eda.subs.ValueException;
import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.utilities.uima.UimaUtils;
import eu.excitementproject.eop.common.utilities.uima.UimaUtilsException;

public class P1EdaVisualizer implements Visualizer {

		protected HashMap<String, String> hashPOS;
		protected HashMap<String, HashMap<String, String>> hashRel;
		protected HashMap<String, String> hashTEEntities;
		
		protected String strDocText;
		protected String strDocData;
		protected String strDocEntities;
		
		protected String docAlignmentData;
		protected String strRelationEntities;
		protected HashMap<String, String> hashAlignmentData;
		protected HashMap<String, String> hashAlignmentEntities;
		protected String strRelationData;
		protected  StringBuilder strHtml;
		
		public P1EdaVisualizer() {
			init();
		}
		
		/* (non-Javadoc)
		 * @see eu.excitementproject.eop.alignmentedas.p1eda.visualization.Visualizer#generateHTML(eu.excitementproject.eop.alignmentedas.p1eda.TEDecisionWithAlignment)
		 * 
 		 * Note: There might be problems with the generated html in case the strings in the given JCas (i.e., the text and the hypothesis) contain empty spaces. 
		 * It is high recommended to trim these spaces while generating the provided JCas.

		 */
		public String generateHTML(TEDecisionWithAlignment decision) throws VisualizerGenerationException
		{
			init();
			JCas jCas = decision.getJCasWithAlignment();
			Vector<FeatureValue> featureValues = decision.getFeatureVector();
			DecisionLabel label = decision.getDecision();
			Double confidence = decision.getConfidence();
			
			try {
				return generateHTML(jCas, label.toString(), confidence.toString(), featureValues);
			} catch (ValueException e) {
				throw new VisualizerGenerationException(e);
			}
		}
		
		/* (non-Javadoc)
		 * @see eu.excitementproject.eop.alignmentedas.p1eda.visualization.Visualizer#generateHTML(org.apache.uima.jcas.JCas)
		 * 
		 * 
		 * Note: There might be problems with the generated html, in case the strings in the given JCas (i.e., the text and the hypothesis) contain empty spaces. 
		 * It is high recommended to trim these spaces while generating the provided JCas.
		 */
		public String generateHTML(JCas jCas) throws VisualizerGenerationException
		{
			try {
				return generateHTML(jCas,null, null, null);
			} catch (ValueException e) {
				throw new VisualizerGenerationException(e);
			}
		}
		
		
		/**
		 * Generates an html string, which visualizes the various annotations and alignments defined in the JCas (with filtering options), and some details on the entailment decision
		 * 
		 * @param jCas JCas object, composed of text, hypothesis and their annotations (e.g., part-of-speech, dependency relations, alignments)
		 * @param strDecisionLabel A description of the entailment decision 
		 * @param confidence The confidence of the entailment decision
		 * @param featureValues A list of the features and their values, used for the entailment decision. 
		 * @return an html string, which visualizes the various annotations and alignments defined in the JCas, the features, and the entailment decision.
		 * @throws ValueException
		 */
		protected String generateHTML(JCas jCas,String strDecisionLabel, String confidence , Vector<FeatureValue> featureValues ) throws ValueException
		{
			
			init();
			
			// define the colors of the entity annotations and their relations
			String alignmentEntityColor = "#88ccFf";
			String entityPOSColor = "#7fffa2";
			String relationAlignColor = "blue";
			String relationDEPColor = "green";
			
			
			// basic containers for the entities, the relations and the alignments
			HashMap<String,Boolean> entities = new HashMap<String,Boolean>();
			HashMap<String,Boolean> relationEntities = new HashMap<String,Boolean>();
			HashMap<String,Boolean> alignmentEntities = new HashMap<String,Boolean>();
			
			// basic java-script variables 
			strDocEntities = "var collData = { entity_types: [ \r\n";
			strDocText = "var docData = { \r\n";
			strDocData = "docData['entities'] = [ \r\n";
			strRelationEntities = "collData['relation_types'] = [ \r\n";
			strRelationData = "docData['relations'] = [ \r\n";
			strHtml = new StringBuilder();
			
			int countInstances = 0;
			int countRelation = 0;
			try {
				
				// Get the text and the hypothesis data from the JCas
				JCas jCasText = jCas.getView("TextView");
				JCas jCasHypothesis = jCas.getView("HypothesisView");
				
				String txText = jCasText.getDocumentText().replaceAll("'", "`");
				String hpText = jCasHypothesis.getDocumentText().replaceAll("'", "`");

				strDocText += " text     : '"+txText+"\\r\\n"+hpText+"'\r\n";
				
				int TextSize = txText.length()+2;
				Collection<AnnotationFS> col = CasUtil.selectAll(jCasText.getCas());
				Collection<AnnotationFS> colH = CasUtil.selectAll(jCasHypothesis.getCas());

				checkAllTypes(col);
				checkAllTypes(colH);
				
				hashPOS = new HashMap<String, String>();
				hashRel = new HashMap<String, HashMap<String, String>>();
				//check if there is Dependency
				boolean hasDependency = getIfThereIsDependency(col);
				
				if(hasDependency)
				{
					
					
					//for text sentence
					updateEntitiesAndRelations(col, 0);
					//for hypothesis sentence
					updateEntitiesAndRelations(colH, TextSize);
					
					// adding the POS collection and data
					for (String entity : hashPOS.keySet()) {
						String strVal = hashPOS.get(entity);
						if(!entities.containsKey(strVal))
						{
							if(entities.keySet().size()!=0)
								strDocEntities+=", \r\n";
							
							strDocEntities+=" { \r\n";
							strDocEntities+=" type   : '"+strVal+"', \r\n";
							strDocEntities+=" labels : ['"+strVal+"'], \r\n";
							strDocEntities+=" bgColor: '"+entityPOSColor+"', \r\n";
							strDocEntities+=" borderColor: 'darken' \r\n";
							strDocEntities+="} \r\n";
							
							entities.put(strVal, true);
						}
						int indexOfS = entity.indexOf("S");
						String  begin = entity.substring(1,indexOfS);
						String  end = entity.substring(indexOfS+1);
						strDocData += " ['"+entity+"', '"+strVal+"', [["+begin+", "+end+"]]], \r\n";
					}
					
					// adding the relations collection and data
					for (String fromRelation : hashRel.keySet()) {
						HashMap<String, String> hashTo = hashRel.get(fromRelation);
						for (String toRelation : hashTo.keySet()) {
							String type = hashTo.get(toRelation);
							
							if(!relationEntities.containsKey(type))
							{
								if(relationEntities.keySet().size()!=0)
									strRelationEntities+=", \r\n";
								
								strRelationEntities += " { \r\n";
								strRelationEntities += "     type     : '"+type+"', \r\n";
								strRelationEntities += "     labels   : ['"+type+"'], \r\n";
								strRelationEntities += "     dashArray: '3,3', \r\n";
								strRelationEntities += "     color    : '"+relationDEPColor+"', \r\n";
								strRelationEntities += "     args     : [ \r\n";
								strRelationEntities += "            {role: 'From'},\r\n";
								strRelationEntities += "           {role: 'To'}\r\n";
								strRelationEntities += "        ] \r\n";
								strRelationEntities += " } \r\n";
								relationEntities.put(type, true);
							}
							
							strRelationData += " ['R"+(++countRelation)+"', '"+type+"', [['From', '"+fromRelation+"'], ['To', '"+toRelation+"']]], \r\n";
						}
					}
					
					
				}
				else
				{
					//for text sentence
					for (AnnotationFS annotationFS : col) {
						Type type = annotationFS.getType();
						//String typeShortName = type.getShortName();
						int begin = annotationFS.getBegin();
						int end = annotationFS.getEnd();
						String strVal = "";
						/*if(typeShortName.equals("Lemma"))
						{
							strVal=((Lemma)annotationFS).getValue();
							if(strVal.contains("'"))
								strVal=strVal.replaceAll("'", "\\\\'");
		
							if(!entities.containsKey(strVal))
							{
								if(entities.keySet().size()!=0)
									strDocEntities+=", ";
								
								strDocEntities+=" { ";
								strDocEntities+=" type   : '"+strVal+"', ";
								strDocEntities+=" labels : ['"+strVal+"'], ";
								strDocEntities+=" bgColor: '#7fa2ff', ";
								strDocEntities+=" borderColor: 'darken' ";
								strDocEntities+="} ";
								entities.put(strVal, true);
							}
							strDocData += " ['T"+(++countInstances)+"', '"+strVal+"', [["+begin+", "+end+"]]], ";
						}*/
						if(type.toString().startsWith("de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos"))
						{
							strVal=((POS)annotationFS).getPosValue();
							if(!entities.containsKey(strVal))
							{
								if(entities.keySet().size()!=0)
									strDocEntities+=", \r\n";
								
								strDocEntities+=" { \r\n";
								strDocEntities+=" type   : '"+strVal+"', \r\n";
								strDocEntities+=" labels : ['"+strVal+"'], \r\n";
								strDocEntities+=" bgColor: '"+entityPOSColor+"', \r\n";
								strDocEntities+=" borderColor: 'darken' \r\n";
								strDocEntities+="} \r\n";
								
								entities.put(strVal, true);
							}
							strDocData += " ['T"+(++countInstances)+"', '"+strVal+"', [["+begin+", "+end+"]]], \r\n";
						}
					}
					
					//for hypothesis sentence
					for (AnnotationFS annotationFS : colH) {
						int begin = annotationFS.getBegin()+TextSize;
						int end = annotationFS.getEnd()+TextSize;
						String strVal = "";
						Type type = annotationFS.getType();
						//String typeShortName = type.getShortName();
						if(type.toString().startsWith("de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos"))
						{
							strVal=((POS)annotationFS).getPosValue();
							if(!entities.containsKey(strVal))
							{
								if(entities.keySet().size()!=0)
									strDocEntities+=", \r\n";
								strDocEntities+=" { \r\n";
								strDocEntities+=" type   : '"+strVal+"', \r\n";
								strDocEntities+=" labels : ['"+strVal+"'], \r\n";
								strDocEntities+=" bgColor: '"+entityPOSColor+"', \r\n";
								strDocEntities+=" borderColor: 'darken' \r\n";
								strDocEntities+="} \r\n";
								entities.put(strVal, true);
							}
							strDocData += " ['T"+(++countInstances)+"', '"+strVal+"', [["+begin+", "+end+"]]], \r\n";
							
						}
						
					}
				}
				
				
				
				// get "Link" type annotations back..
				for (Link l : JCasUtil.select(jCasHypothesis, Link.class))
				{
					
					// you can access Link, as normal, annotation. Of course.
					int tBegin = l.getTSideTarget().getBegin();
					int hBegin = l.getHSideTarget().getBegin()+TextSize;
					int tEnd = l.getTSideTarget().getEnd();
					int hEnd = l.getHSideTarget().getEnd()+TextSize;
					String tText = l.getTSideTarget().getCoveredText();
					String hText = l.getHSideTarget().getCoveredText();

					if(tText.contains("'"))
						tText=tText.replaceAll("'", "`");
					if(hText.contains("'"))
						hText=hText.replaceAll("'", "`");
					
					if(!entities.containsKey(tText+"Sred"))
					{
						if(entities.keySet().size()!=0)
							strDocEntities+=", \r\n";
						
						strDocEntities+=" { \r\n";
						strDocEntities+=" type   : '"+tText+"Sred"+"', \r\n";
						strDocEntities+=" labels : ['"+tText+"'], \r\n";
						strDocEntities+=" bgColor: '"+alignmentEntityColor+"', \r\n";
						strDocEntities+=" borderColor: 'darken' \r\n";
						strDocEntities+="} \r\n";
						
						entities.put(tText, true);
					}
					if(!alignmentEntities.keySet().contains(tText+tBegin+"S"+tEnd))
					{
						String key = "TE"+(++countInstances);
						String value = " ['"+key+"', '"+tText+"Sred"+"', [["+tBegin+", "+tEnd+"]]], \r\n";
						if(!hashTEEntities.keySet().contains(key))
							hashTEEntities.put(key, value);
						docAlignmentData += value;
						alignmentEntities.put(tText+tBegin+"S"+tEnd, true);
					}
					
					
					if(!entities.containsKey(hText+"Sred"))
					{
						if(entities.keySet().size()!=0)
							strDocEntities+=", \r\n";
						
						strDocEntities+=" { \r\n";
						strDocEntities+=" type   : '"+hText+"Sred"+"', \r\n";
						strDocEntities+=" labels : ['"+hText+"'], \r\n";
						strDocEntities+=" bgColor: '"+alignmentEntityColor+"', \r\n";
						strDocEntities+=" borderColor: 'darken' \r\n";
						strDocEntities+="} \r\n";
						
						entities.put(hText, true);
					}
					if(!alignmentEntities.keySet().contains(hText+hBegin+"S"+hEnd))
					{
						String key = "TE"+(++countInstances);
						String value = " ['"+key+"', '"+hText+"Sred"+"', [["+hBegin+", "+hEnd+"]]], \r\n";
						if(!hashTEEntities.keySet().contains(key))
							hashTEEntities.put(key, value);
						docAlignmentData += value;
						alignmentEntities.put(hText+hBegin+"S"+hEnd, true);
					}
					
					String relation = l.getID() + " (" + l.getStrength() + ")";
					String []strSplit = l.getID().split("_");
					if(!relationEntities.containsKey(relation))
					{
						if(relationEntities.keySet().size()!=0)
							strRelationEntities+=", \r\n";
						
						strRelationEntities += " { \r\n";
						strRelationEntities += "     type     : '"+relation+"', \r\n";
						strRelationEntities += "     labels   : ['"+relation+"'], \r\n";
						strRelationEntities += "     dashArray: '3,3', \r\n";
						strRelationEntities += "     color    : '"+relationAlignColor+"', \r\n";
						strRelationEntities += "     args     : [ \r\n";
						strRelationEntities += "            {role: 'From'},\r\n";
						strRelationEntities += "           {role: 'To'}\r\n";
						strRelationEntities += "        ] \r\n";
						strRelationEntities += " } \r\n";
						relationEntities.put(relation, true);
					}
					
					if(!hashAlignmentData.keySet().contains(strSplit[0]))
						hashAlignmentData.put(strSplit[0], "docData['relations_"+strSplit[0]+"'] = [ ");
					
					if(!hashAlignmentEntities.keySet().contains(strSplit[0]))
						hashAlignmentEntities.put(strSplit[0], "docData['alignment_entity_"+strSplit[0]+"'] = [ ");
					
					
					
					
					hashAlignmentEntities.put(strSplit[0], hashAlignmentEntities.get(strSplit[0]) + hashTEEntities.get("TE"+(countInstances-1)));
					hashAlignmentEntities.put(strSplit[0], hashAlignmentEntities.get(strSplit[0]) + hashTEEntities.get("TE"+(countInstances)));
					
					String strRelationInstance =  " ['RE"+(++countRelation)+"', '"+relation+"', [['From', 'TE"+(countInstances-1)+"'], ['To', 'TE"+countInstances+"']]], \r\n";
					hashAlignmentData.put(strSplit[0], hashAlignmentData.get(strSplit[0]) + strRelationInstance);					
				}
				
				
				strDocEntities += " ] \r\n";
				strDocEntities += " }; \r\n";	
				strDocData += " ]; \r\n";
				strDocText += " }; \r\n";
				
				
				
				docAlignmentData += " ]; \r\n";
				strRelationEntities += "  ]; \r\n";
				strRelationData += "  ]; \r\n";
				
				
				
				
				for (String strBlock : hashAlignmentData.keySet()) {
					hashAlignmentData.put(strBlock, hashAlignmentData.get(strBlock) + "  ]; ");
				}
				
				for (String strBlock : hashAlignmentEntities.keySet()) {
						hashAlignmentEntities.put(strBlock, hashAlignmentEntities.get(strBlock) + "  ]; ");
				}
				
				
				
				// Generate the html string
				
				strHtml.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3c.org/TR/1999/REC-html401-19991224/loose.dtd\">\r\n");
				strHtml.append(" <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\r\n");
				strHtml.append(" <HTML lang=\"en-GB\" lang=\"en-GB\" xml:lang=\"en-GB\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n");
				strHtml.append(" <HEAD>  \r\n");
				strHtml.append(" <META http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\r\n");     
				strHtml.append(" <TITLE>EOP Visualizar</TITLE>\r\n");
				
				
				strHtml.append("<STYLE>" +  GetCss() + " body { width:100%;  text-align: center;  }</STYLE>\r\n");
				//strHtml.append(" <LINK href=\"style-vis.css\"\r\n"; 
				//strHtml.append(" rel=\"stylesheet\" type=\"text/css\">         \r\n";
				strHtml.append(" <SCRIPT language=\"javascript\" type=\"text/javascript\">(function (a) { var b = a.documentElement, c, d, e = [], f = [], g = {}, h = {}, i = a.createElement(\"script\").async === true || \"MozAppearance\" in a.documentElement.style || window.opera; var j = window.head_conf && head_conf.head || \"head\", k = window[j] = window[j] || function () { k.ready.apply(null, arguments) }; var l = 0, m = 1, n = 2, o = 3; i ? k.js = function () { var a = arguments, b = a[a.length - 1], c = []; t(b) || (b = null), s(a, function (d, e) { d != b && (d = r(d), c.push(d), x(d, b && e == a.length - 2 ? function () { u(c) && p(b) } : null)) }); return k } : k.js = function () { var a = arguments, b = [].slice.call(a, 1), d = b[0]; if (!c) { f.push(function () { k.js.apply(null, a) }); return k } d ? (s(b, function (a) { t(a) || w(r(a)) }), x(r(a[0]), t(d) ? d : function () { k.js.apply(null, b) })) : x(r(a[0])); return k }, k.ready = function (a, b) { if (a == \"dom\") { d ? p(b) : e.push(b); return k } t(a) && (b = a, a = \"ALL\"); var c = h[a]; if (c && c.state == o || a == \"ALL\" && u() && d) { p(b); return k } var f = g[a]; f ? f.push(b) : f = g[a] = [b]; return k }, k.ready(\"dom\", function () { c && u() && s(g.ALL, function (a) { p(a) }), k.feature && k.feature(\"domloaded\", true) }); function p(a) { a._done || (a(), a._done = 1) } function q(a) { var b = a.split(\"/\"), c = b[b.length - 1], d = c.indexOf(\"?\"); return d != -1 ? c.substring(0, d) : c } function r(a) { var b; if (typeof a == \"object\") for (var c in a) a[c] && (b = { name: c, url: a[c] }); else b = { name: q(a), url: a }; var d = h[b.name]; if (d && d.url === b.url) return d; h[b.name] = b; return b } function s(a, b) { if (a) { typeof a == \"object\" && (a = [].slice.call(a)); for (var c = 0; c < a.length; c++) b.call(a, a[c], c) } } function t(a) { return Object.prototype.toString.call(a) == \"[object Function]\" } function u(a) { a = a || h; var b = false, c = 0; for (var d in a) { if (a[d].state != o) return false; b = true, c++ } return b || c === 0 } function v(a) { a.state = l, s(a.onpreload, function (a) { a.call() }) } function w(a, b) { a.state || (a.state = m, a.onpreload = [], y({ src: a.url, type: \"cache\" }, function () { v(a) })) } function x(a, b) { if (a.state == o && b) return b(); if (a.state == n) return k.ready(a.name, b); if (a.state == m) return a.onpreload.push(function () { x(a, b) }); a.state = n, y(a.url, function () { a.state = o, b && b(), s(g[a.name], function (a) { p(a) }), d && u() && s(g.ALL, function (a) { p(a) }) }) } function y(c, d) { var e = a.createElement(\"script\"); e.type = \"text/\" + (c.type || \"javascript\"), e.src = c.src || c, e.async = false, e.onreadystatechange = e.onload = function () { var a = e.readyState; !d.done && (!a || /loaded|complete/.test(a)) && (d(), d.done = true) }, b.appendChild(e) } setTimeout(function () { c = true, s(f, function (a) { a() }) }, 0); function z() { d || (d = true, s(e, function (a) { p(a) })) } window.addEventListener ? (a.addEventListener(\"DOMContentLoaded\", z, false), window.addEventListener(\"onload\", z, false)) : window.attachEvent && (a.attachEvent(\"onreadystatechange\", function () { a.readyState === \"complete\" && z() }), window.frameElement == null && b.doScroll && function () { try { b.doScroll(\"left\"), z() } catch (a) { setTimeout(arguments.callee, 1); return } } (), window.attachEvent(\"onload\", z)), !a.readyState && a.addEventListener && (a.readyState = \"loading\", a.addEventListener(\"DOMContentLoaded\", handler = function () { a.removeEventListener(\"DOMContentLoaded\", handler, false), a.readyState = \"complete\" }, false)) })(document)</SCRIPT>\r\n");  
				strHtml.append(" </HEAD>\r\n");
				strHtml.append(" <BODY>\r\n");
				
				strHtml.append(" <DIV id=\"content\" style=\"margin: 0px auto; width:96%;\">\r\n");
				
				strHtml.append(" <h2 id=\"header\">Entailment Visualization</h2>");
				if (strDecisionLabel != null) {
					strHtml.append("<h3>Decision: " + strDecisionLabel);
					if (confidence != null) {
						strHtml.append(", Confidence: " + confidence);
					}
					strHtml.append("</h3>");
				}
				
				strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-call\">\r\n");
				strHtml.append(" head.ready(function() {\r\n");
				strHtml.append("     Util.embed(\r\n");
				strHtml.append("         '${DIV_ID}',\r\n");
				strHtml.append(" collData,\r\n");
				strHtml.append(" docData,\r\n");
				strHtml.append(" webFontURLs\r\n");
				strHtml.append("     );\r\n");
				strHtml.append(" });\r\n");
				strHtml.append(" </CODE></PRE>\r\n");
				strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-entity-coll\"> \r\n");
				strHtml.append( strDocEntities );
				strHtml.append(" </CODE></PRE>\r\n");
				strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-text-doc\"> \r\n");
				strHtml.append( strDocText );
				strHtml.append(" </CODE></PRE>\r\n");
				
				strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-init-doc\">\r\n"); 
				strHtml.append("  docData['entities'] = [ ];\r\n");
				strHtml.append("  docData['relations'] = [ ];\r\n");
				strHtml.append(" </CODE></PRE>\r\n");
				
				strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-entity-doc\"> \r\n");
				strHtml.append( strDocData );
				strHtml.append(" </CODE></PRE>\r\n");
				strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-alignment-entity-doc\"> \r\n");
				strHtml.append(docAlignmentData);
				strHtml.append(" </CODE></PRE>\r\n");
				
				strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-relation-coll\"> \r\n");
				strHtml.append( strRelationEntities );
				strHtml.append(" </CODE></PRE>\r\n");
				strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-relation-doc\"> \r\n");
				strHtml.append(strRelationData);
				strHtml.append(" </CODE></PRE>\r\n");
				
				
				
				for (String strBlock : hashAlignmentData.keySet()) {
					strHtml.append(" <PRE style=\"display:none;\"><CODE id=\"embedding-relation-"+strBlock+"-doc\"> \r\n");
					
					strHtml.append( hashAlignmentEntities.get(strBlock) + "\r\n");
					strHtml.append( hashAlignmentData.get(strBlock));
					strHtml.append(" </CODE></PRE>\r\n");
					///////XXXXXXXXXXX
					//hashAlignmentData.put(strBlock, hashAlignmentData.get(strBlock) + "  ]; ");
				}
				
				//strHtml.append(" <DIV id=\"embedding-relation-example\"></DIV>\r\n");
				strHtml.append(" <DIV id=\"embedding-live-example\"></DIV>\r\n");
				strHtml.append("<br>\r\n");
				strHtml.append(" <div style='width:100%;  text-align: center;'>");
				strHtml.append(" <table style='width:500px; margin: 0px auto;'>\r\n");
				if (hasDependency)
					strHtml.append("<tr><td><b>Annotations: </b></td>\r\n");
				else
					strHtml.append("<tr style=\"display:none\"><td><b>Annotations: </b></td>\r\n");
				strHtml.append("<td style='background-color: " + relationDEPColor + ";'><input id=\"cb_DEP\" type=\"checkbox\" onclick='Update();' checked=\"checked\" />Dependency</td>\r\n");
				strHtml.append("<td style='background-color: " + entityPOSColor + ";'><input id=\"cb_POS\" type=\"checkbox\" onclick='Update();' checked=\"checked\" />POS</td>\r\n");
				strHtml.append("</tr>\r\n");
					
				if (!hashAlignmentData.keySet().isEmpty()) {
					strHtml.append("<tr><td><b>Alignments:</b></td>\r\n");
					for (String strBlock : hashAlignmentData.keySet()) 
						strHtml.append("    <td style='background-color: " + alignmentEntityColor + ";'><input id=\"cb_"+strBlock+"\" type=\"checkbox\" onclick=\"javascript:Update();\" checked=\"checked\" />"+strBlock+"</td>\r\n");
				} else
					strHtml.append("<tr style=\"display:none\"><td><b>Alignments: </b></td>\r\n");				
				strHtml.append("</tr></table><br/><hr/>\r\n");
				strHtml.append("</div>");
				
				if(featureValues!=null)
				{
					strHtml.append("<div><h3>Extracted Features</h3></div>   ");
					
					strHtml.append("<style>.datagrid table { border-collapse: collapse; text-align: left; width: 100%; } .datagrid {font: normal 12px/150% Arial, Helvetica, sans-serif; background: #fff; overflow: hidden; border: 3px solid #006699; -webkit-border-radius: 11px; -moz-border-radius: 11px; border-radius: 11px; }.datagrid table td, .datagrid table th { padding: 4px 4px; }.datagrid table thead th {background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #006699), color-stop(1, #00557F) );background:-moz-linear-gradient( center top, #006699 5%, #00557F 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#006699', endColorstr='#00557F');background-color:#006699; color:#FFFFFF; font-size: 15px; font-weight: bold; border-left: 2px solid #E1EEF4; } .datagrid table thead th:first-child { border: none; }.datagrid table tbody td { color: #00496B; border-left: 1px solid #E1EEF4;font-size: 14px;font-weight: normal; }.datagrid table tbody .alt td { background: #E1EEF4; color: #00496B; }.datagrid table tbody td:first-child { border-left: none; }.datagrid table tbody tr:last-child td { border-bottom: none; }.datagrid table tfoot td div { border-top: 1px solid #006699;background: #E1EEF4;} .datagrid table tfoot td { padding: 0; font-size: 12px } .datagrid table tfoot td div{ padding: 2px; }.datagrid table tfoot td ul { margin: 0; padding:0; list-style: none; text-align: right; }.datagrid table tfoot  li { display: inline; }.datagrid table tfoot li a { text-decoration: none; display: inline-block;  padding: 2px 8px; margin: 1px;color: #FFFFFF;border: 1px solid #006699;-webkit-border-radius: 3px; -moz-border-radius: 3px; border-radius: 3px; background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #006699), color-stop(1, #00557F) );background:-moz-linear-gradient( center top, #006699 5%, #00557F 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#006699', endColorstr='#00557F');background-color:#006699; }.datagrid table tfoot ul.active, .datagrid table tfoot ul a:hover { text-decoration: none;border-color: #006699; color: #FFFFFF; background: none; background-color:#00557F;}div.dhtmlx_window_active, div.dhx_modal_cover_dv { position: fixed !important; }</style>");
					
					strHtml.append(" <div style='width:100%;  text-align: center;'>");
					strHtml.append("<div style='width: 400px; margin: 0px auto;' class=\"datagrid\"><table>");
					strHtml.append("<thead><tr><th style='width: 70%;'>Feature</th><th>Value</th></tr></thead><tbody>");
					
					
					boolean bRow = true;
					for (FeatureValue featureValue : featureValues) {
						if(bRow)
							strHtml.append("<tr>");
						else 
							strHtml.append("<tr class=\"alt\">");
						
						strHtml.append("<td>"+featureValue.getFeatureName()+"</td><td>"+featureValue.getDoubleValue()+"</td></tr>"); 
						bRow = !bRow;
						
					}
					strHtml.append("</tbody></table></div></div><br/><br/>");
					
				
				}
				
				strHtml.append(" <DIV id=\"live-io\" style=\"display:none;\">\r\n");
				strHtml.append(" <P><TEXTAREA id=\"coll-input\" style=\"border: 2px inset currentColor; border-image: none; width: 40%; height: 400px; font-size: 11px; float: left; display: block;\" placeholder=\"Enter JSON for the collection object here...\">Enter JSON for the collection object here...</TEXTAREA>\r\n");
				strHtml.append(" <TEXTAREA id=\"doc-input\" style=\"border: 2px inset currentColor; border-image: none; width: 55%; height: 400px; font-size: 11px; float: right; display: block;\" placeholder=\"Enter JSON for the document object here...\">Enter JSON for the document object here...</TEXTAREA></P></DIV>\r\n");
				strHtml.append(" <STYLE type=\"text/css\">\r\n");
				strHtml.append(" text { font-size: 15px; }\r\n");
				strHtml.append(" .span text { font-size: 10px; }\r\n");
				strHtml.append(" .arcs text { font-size: 9px; }\r\n");
				strHtml.append(" </STYLE>\r\n");
	 
				
				strHtml.append(" <SCRIPT type=\"text/javascript\">\r\n");
				
				strHtml.append(" var packJSON = function (s) { \r\n");
			    strHtml.append("     s = s.replace(/(\\{[^\\{\\}\\[\\]]*\\})/g,\r\n");
				strHtml.append("                       function (a, b) { return b.replace(/\\s+/g, ' '); });\r\n");
			    strHtml.append("     s = s.replace(/(\\[(?:[^\\[\\]\\{\\}]|\\[[^\\[\\]\\{\\}]*\\])*\\])/g,\r\n");
				strHtml.append("                       function (a, b) { return b.replace(/\\s+/g, ' '); });\r\n");
				strHtml.append("     return s;\r\n");
				strHtml.append(" } \r\n");
				
				
				strHtml.append("     var bratLocation = 'http://weaver.nlplab.org/~brat/demo/v1.3';\r\n");
				strHtml.append("     head.js(\r\n");
				strHtml.append("         bratLocation + '/client/lib/jquery.min.js',\r\n");
				strHtml.append("         bratLocation + '/client/lib/jquery.svg.min.js',\r\n");
				strHtml.append("         bratLocation + '/client/lib/jquery.svgdom.min.js',\r\n");
				strHtml.append("         bratLocation + '/client/src/configuration.js',\r\n");
				strHtml.append("         bratLocation + '/client/src/util.js',\r\n");
				strHtml.append("         bratLocation + '/client/src/annotation_log.js',\r\n");
				strHtml.append("         bratLocation + '/client/lib/webfont.js',\r\n");
				strHtml.append("         bratLocation + '/client/src/dispatcher.js',\r\n");
				strHtml.append(" 		 bratLocation + '/client/src/url_monitor.js',\r\n");
				strHtml.append("         bratLocation + '/client/src/visualizer.js'\r\n");
				strHtml.append("     );\r\n");
				strHtml.append("     var webFontURLs = [\r\n");
				strHtml.append("         bratLocation + '/static/fonts/Astloch-Bold.ttf',\r\n");
				strHtml.append("         bratLocation + '/static/fonts/PT_Sans-Caption-Web-Regular.ttf',\r\n");
				strHtml.append("         bratLocation + '/static/fonts/Liberation_Sans-Regular.ttf'\r\n");
				strHtml.append("     ];\r\n");
				strHtml.append("     var liveDispatcher;\r\n");
				strHtml.append("     head.ready(function () {\r\n");
				strHtml.append("         document.getElementById(\"cb_POS\").disabled = true;\r\n");
				strHtml.append("         eval($('#embedding-entity-coll').text());\r\n");
				strHtml.append("         eval($('#embedding-text-doc').text());\r\n");
				strHtml.append("         eval($('#embedding-entity-doc').text());\r\n");
				strHtml.append("      	 eval($('#embedding-alignment-entity-doc').text());");
				strHtml.append("      	 docData['entities'] = docData['entities'].concat(docData['alignment_entity']);");

				strHtml.append("         eval($('#embedding-relation-coll').text());\r\n");
				strHtml.append("         eval($('#embedding-relation-doc').text());\r\n");
				
				for (String strBlock : hashAlignmentData.keySet()) {
					strHtml.append(" 	 	eval($('#embedding-relation-"+strBlock+"-doc').text());\r\n");
					strHtml.append(" 	 	docData['relations'] = docData['relations'].concat(docData['relations_"+strBlock+"']);\r\n");
				}
				//strHtml.append("         Util.embed('embedding-relation-example', $.extend({}, collData),\r\n");
				//strHtml.append("                 $.extend({}, docData), webFontURLs);   \r\n");
				
				
				strHtml.append("         var collInput = $('#coll-input');\r\n");
				strHtml.append("         var docInput = $('#doc-input');\r\n");
				strHtml.append("         var liveDiv = $('#embedding-live-example');\r\n");

				strHtml.append("         liveDispatcher = Util.embed('embedding-live-example',\r\n");
				strHtml.append("                 $.extend({ 'collection': null }, collData),\r\n");
				strHtml.append("                 $.extend({}, docData), webFontURLs);\r\n");
		        
				strHtml.append("         var renderError = function () {\r\n");
				strHtml.append("             collInput.css({ 'border': '2px solid red' });\r\n");
				strHtml.append("             docInput.css({ 'border': '2px solid red' });\r\n");
				strHtml.append("         };\r\n");
				strHtml.append("         liveDispatcher.on('renderError: Fatal', renderError);\r\n");
				strHtml.append("         var collInputHandler = function () {\r\n");
				strHtml.append("             var collJSON;\r\n");
				strHtml.append("             try {\r\n");
				strHtml.append("                 collJSON = JSON.parse(collInput.val());\r\n");
				strHtml.append("                 collInput.css({ 'border': '2px inset' });\r\n");
				strHtml.append("             } catch (e) {\r\n");
				strHtml.append("                 collInput.css({ 'border': '2px solid red' });\r\n");
				strHtml.append("                 return;\r\n");
				strHtml.append("             }\r\n");

				strHtml.append("             try {\r\n");
				strHtml.append("                 liveDispatcher.post('collectionLoaded',[$.extend({ 'collection': null }, collJSON)]);\r\n");
				strHtml.append("                 docInput.css({ 'border': '2px inset' });\r\n");
				strHtml.append("             } catch (e) {\r\n");
				strHtml.append("                 console.error('collectionLoaded went down with:', e);\r\n");
				strHtml.append("                 collInput.css({ 'border': '2px solid red' });\r\n");
				strHtml.append("             }\r\n");
				strHtml.append("         };\r\n");

				strHtml.append("         var docInputHandler = function () {\r\n");
				strHtml.append("             var docJSON;\r\n");
				strHtml.append("             try {\r\n");
				strHtml.append("                 docJSON = JSON.parse(docInput.val());\r\n");
				strHtml.append("                 docInput.css({ 'border': '2px inset' });\r\n");
				strHtml.append("             } catch (e) {\r\n");
				strHtml.append("                 docInput.css({ 'border': '2px solid red' });\r\n");
				strHtml.append("                 return;\r\n");
				strHtml.append("             }\r\n");

				strHtml.append("             try {\r\n");
				strHtml.append("                 liveDispatcher.post('requestRenderData', [$.extend({}, docJSON)]);\r\n");
				strHtml.append("                 collInput.css({ 'border': '2px inset' });\r\n");
				strHtml.append("             } catch (e) {\r\n");
				strHtml.append("                 console.error('requestRenderData went down with:', e);\r\n");
				strHtml.append("                 collInput.css({ 'border': '2px solid red' });\r\n");
				strHtml.append("             }\r\n");
				strHtml.append("         };\r\n");

				strHtml.append("         var collJSON = JSON.stringify(collData, undefined, '    ');\r\n");
		        strHtml.append("         docJSON = JSON.stringify(docData, undefined, '    ')\r\n");
		        
		        strHtml.append("         collInput.text(packJSON(collJSON));\r\n");
		        strHtml.append("         docInput.text(packJSON(docJSON));\r\n");

		        strHtml.append("         var listenTo = 'propertychange keyup input paste';\r\n");
		        strHtml.append("         collInput.bind(listenTo, collInputHandler);\r\n");
		        strHtml.append("         docInput.bind(listenTo, docInputHandler);\r\n");
				
				
				strHtml.append("     });\r\n");
				strHtml.append(" </SCRIPT></DIV>\r\n");
				

				strHtml.append(" 	<div>\r\n");
					
				strHtml.append(" 	  <script language=\"javascript\" type=\"text/javascript\">\r\n");
				strHtml.append(" 	    function Update() {\r\n");
				strHtml.append("           docData = \"\";\r\n");
				
				strHtml.append("      	   eval($('#embedding-entity-coll').text());\r\n");
				strHtml.append("      	   eval($('#embedding-relation-coll').text());\r\n");
				
				strHtml.append("      	   eval($('#embedding-text-doc').text());\r\n");
				strHtml.append("      	   eval($('#embedding-init-doc').text());\r\n");
				strHtml.append("      	   var is_DEP = document.getElementById(\"cb_DEP\").checked;\r\n");
				strHtml.append("      	   var chkPOS = document.getElementById(\"cb_POS\");\r\n");
				strHtml.append("      	   var is_POS = chkPOS.checked;\r\n");
				strHtml.append("      	   if (is_DEP) {\r\n");
				strHtml.append("      	         eval($('#embedding-relation-doc').text());\r\n");
				strHtml.append("      	         eval($('#embedding-entity-doc').text());\r\n");
				strHtml.append("      	         chkPOS.checked = true;\r\n");
				strHtml.append("      	         chkPOS.disabled = true;\r\n");
				strHtml.append("      	   } else if (is_POS) {\r\n");
				strHtml.append("      	   		 eval($('#embedding-entity-doc').text());\r\n");
				strHtml.append("      	   		 chkPOS.disabled = false;\r\n");
				strHtml.append("      	   }\r\n");
				
				
			for (String strBlock : hashAlignmentData.keySet()) {
				strHtml.append(" 	        var is_"+strBlock+" = document.getElementById(\"cb_"+strBlock+"\").checked;\r\n");
				strHtml.append(" 	        if (is_"+strBlock+") {\r\n");
				strHtml.append(" 	            eval($('#embedding-relation-"+strBlock+"-doc').text());\r\n");
				strHtml.append(" 	            docData['relations'] = docData['relations'].concat(docData['relations_"+strBlock+"']);\r\n");
				strHtml.append(" 	            docData['entities'] = docData['entities'].concat(docData['alignment_entity_"+strBlock+"']);\r\n");
				strHtml.append(" 	        }\r\n");
			}
				
				strHtml.append(" 	        var docJSON = JSON.stringify(docData, undefined, '    ');\r\n");
				strHtml.append(" 	        var docInput = $('#doc-input');\r\n");
				strHtml.append(" 	        docInput.text(packJSON(docJSON));\r\n");
				strHtml.append(" 	        docJSON = JSON.parse(docInput.val());\r\n");
				strHtml.append(" 	        liveDispatcher.post('requestRenderData', [$.extend({}, docJSON)]);\r\n");					     
				strHtml.append(" 	    }\r\n");
				strHtml.append(" 	 </script>\r\n");
				strHtml.append("   </div>\r\n");
				
				
				strHtml.append("</BODY></HTML>\r\n");

				
			} catch (CASException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			
			return strHtml.toString();
		}
		
		
		private static String GetCss() {
			String ret = "";
			ret += "@font-face {font-family: Liberation Sans;src: local(\"Liberation Sans\"), local(\"Liberation-Sans\"), url(static/fonts/Liberation_Sans-Regular.ttf) format(\"truetype\");font-weight: normal;font-style: normal;}@font-face {font-family: PT Sans Caption;src: local(\"PT Sans Caption\"), local(\"PTSans-Caption\"), url(static/fonts/PT_Sans-Caption-Web-Regular.ttf) format(\"truetype\");font-weight: normal;font-style: normal;}#svg {margin: 34px auto 100px; padding-top: 15px;}.center_wrapper {left: 0px; top: 0px; width: 100%; height: 100%; display: table; position: absolute;}.center_wrapper > div {vertical-align: middle; display: table-cell;}.center_wrapper > div > div {width: 30em; color: rgb(46, 110, 158); font-family: \"Liberation Sans\", Verdana, Arial, Helvetica, sans-serif; font-size: 12px; margin-right: auto; margin-left: auto;}.center_wrapper > div > div h1 {text-align: center; font-size: 14px;}#no_svg_wrapper {display: none;}svg {border: 1px solid rgb(127, 162, 255); border-image: none; width: 100%; height: 1px; font-size: 15px;}svg.reselect {border: 1px solid rgb(255, 51, 51); border-image: none;}text {font-family: \"Liberation Sans\", Verdana, Arial, Helvetica, sans-serif; font-size: 13px;}path {pointer-events: none;}.span text {font-family: \"PT Sans Caption\", sans-serif; font-size: 10px; pointer-events: none; text-anchor: middle;}.span_type_label {font-family: \"PT Sans Caption\", sans-serif; font-size: 11px;}.arc_type_label {font-family: \"PT Sans Caption\", sans-serif; font-size: 11px;}.attribute_type_label .ui-button-text {font-family: \"PT Sans Caption\", sans-serif; font-size: 11px;}.span rect {stroke-width: 0.75;}.glyph {font-family: sans-serif; font-weight: bold; fill: #444444;}.attribute_warning {stroke: red;}.span rect.False_positive {stroke: #ff4141; stroke-width: 2;}.shadow_True_positive {fill: #00ff00;}.shadow_False_positive {fill: #ff4141;}.comment_False_positive#commentpopup {background-color: rgb(255, 65, 65);}.span rect.False_negative {fill: #ffffff; stroke: #c20000; stroke-width: 2;}.shadow_False_negative {fill: #c20000;}.comment_False_negative#commentpopup {background-color: rgb(194, 0, 0);}.span rect.AnnotationError {stroke-width: 1;}.shadow_AnnotationError {fill: #ff0000;}.comment_AnnotationError#commentpopup {background-color: rgb(255, 119, 119);}.span rect.AnnotationWarning {stroke-width: 1;}.shadow_AnnotationWarning {fill: #ff8800;}.comment_AnnotationWarning#commentpopup {background-color: rgb(255, 153, 0);}.shadow_AnnotatorNotes {fill: #3ab7ee;}.comment_AnnotatorNotes#commentpopup {background-color: rgb(215, 231, 238);}.shadow_Normalized {fill: #3aee37;}.comment_Normalized#commentpopup {background-color: rgb(215, 238, 231);}rect.Normalized {stroke-width: 1.5;}.shadow_AnnotationIncomplete {fill: #aaaaaa;}.span rect.AnnotationIncomplete {fill: #ffffff; stroke: #002200; stroke-width: 0.5;}.comment_AnnotationIncomplete#commentpopup {background-color: rgb(255, 255, 119);}.shadow_AnnotationUnconfirmed {fill: #eeeeee;}.span rect.AnnotationUnconfirmed {opacity: 0.5; stroke: #002200; stroke-width: 0.5;}.comment_AnnotationUnconfirmed#commentpopup {background-color: rgb(221, 221, 255);}.span rect.True_positive {}rect.shadow_EditHighlight {fill: #ffff99;}.shadow_EditHighlight_arc {stroke: #ffff99;}.span path {fill: none;}.span path.curly {stroke-width: 0.5;}.span path.boxcross {opacity: 0.5; stroke: black;}.arcs path {fill: none; stroke: #989898; stroke-width: 1;}.arcs .highlight path {opacity: 1; stroke: #000000; stroke-width: 1.5;}.arcs .highlight text {fill: black; stroke: black; stroke-width: 0.5;}.highlight.span rect {stroke-width: 2px;}.span rect.reselect {stroke-width: 2px;}.span rect.reselectTarget {stroke-width: 2px;}.arcs .reselect path {stroke: #ff0000 !important; stroke-width: 2px;}.arcs .reselect text {fill: #ff0000 !important;}.span rect.badTarget {stroke: #f00;}.arcs text {font-family: \"PT Sans Caption\", sans-serif; font-size: 9px; cursor: default; text-anchor: middle;}.background0 {fill: #ffffff; stroke: none;}.background1 {fill: #eeeeee; stroke: none;}.backgroundHighlight {fill: #ffff99; stroke: none;}.sentnum text {fill: #999999; text-anchor: end;}.sentnum path {stroke: #999999; stroke-width: 1px;}.span_cue {fill: #eeeeee !important;}.drag_stroke {stroke: black;}.drag_fill {fill: black;}.dialog {display: none;}#span_free_div {float: left;}#arc_free_div {float: left;}fieldset {border-radius: 5px; border: 1px solid rgb(166, 201, 226); border-image: none; margin-top: 5px; -webkit-border-radius: 5px; -moz-border-radius: 5px;}fieldset legend {border-radius: 3px; color: white; padding-right: 0.5em; padding-left: 0.5em; font-size: 90%; font-weight: bold; background-color: rgb(112, 168, 210); -webkit-border-radius: 3px; -moz-border-radius: 3px;}.label-like {color: rgb(46, 110, 158); font-family: monospace; font-size: 90%; font-weight: bold;}.accesskey {text-decoration: underline;}.shadow {box-shadow: 5px 5px 5px #444444; -moz-box-shadow: 5px 5px 5px #444444; -webkit-box-shadow: 5px 5px 5px #444444;}#span_selected {font-weight: bold;}#arc_origin {font-weight: bold;}#arc_target {font-weight: bold;}#commentpopup {padding: 10px; border-radius: 3px; border: 1px outset rgb(0, 0, 0); border-image: none; left: 0px; top: 0px; color: rgb(0, 0, 0); font-family: \"Liberation Sans\", Verdana, Arial, Helvetica, sans-serif; display: none; position: fixed; z-index: 20; max-width: 80%; opacity: 0.95; box-shadow: 5px 5px 5px #aaaaaa; background-color: rgb(245, 245, 249); -moz-box-shadow: 5px 5px 5px #aaaaaa; -webkit-box-shadow: 5px 5px 5px #aaaaaa; -webkit-border-radius: 3px; -moz-border-radius: 3px;}#more_info_readme {height: 350px;}#readme_container {position: relative;}#more_readme_button {padding: 2px 5px; top: -2px; right: -2px; position: absolute;}.comment_id {color: rgb(51, 51, 51); font-family: monospace; font-size: 75%; vertical-align: top; float: right;}.comment_type {}.comment_text {font-weight: bold;}.comment_type_id_wrapper {padding-right: 2em;}.norm_info_label {font-size: 80%; font-weight: bold;}.norm_info_value {font-size: 80%;}.norm_info_img {margin-left: 1em; float: right;}#search_form select {width: 100%;}.scroll_fset {height: 200px;}.scroll_fset fieldset {height: 100%; -ms-overflow-x: hidden; -ms-overflow-y: hidden;}.scroll_fset {margin-bottom: 2.5em;}.scroll_fset fieldset {padding-bottom: 2em;}.scroll_fset div.scroller {width: 100%; height: 100%; overflow: auto;}#span_highlight_link {float: right;}#arc_highlight_link {float: right;}#viewspan_highlight_link {float: right;}.unselectable {cursor: default; -moz-user-select: -moz-none; -khtml-user-select: none; -webkit-user-select: none; -o-user-select: none; user-select: none;}* {-webkit-tap-highlight-color: rgba(0, 0, 0, 0); -webkit-text-size-adjust: none; select: none;}.span rect.AddedAnnotation {stroke: #ff4141; stroke-width: 2;}.shadow_AddedAnnotation {fill: #ff4141;}.comment_AddedAnnotation#commentpopup {background-color: rgb(255, 204, 204);}.span rect.MissingAnnotation {stroke: #ffffff; stroke-width: 2;}.shadow_MissingAnnotation {opacity: 0.3; fill: #ff4141;}.comment_MissingAnnotation#commentpopup {background-color: rgb(255, 204, 204);}.span rect.MissingAnnotation + text {opacity: 0.5;}.span rect.ChangedAnnotation {stroke: #ffff99; stroke-width: 2;}.shadow_ChangedAnnotation {fill: #ff4141;}.comment_ChangedAnnotation#commentpopup {background-color: rgb(255, 204, 204);}";
			
			return ret;
		}

		private void updateEntitiesAndRelations(Collection<AnnotationFS> col, int Identication) {
			for (AnnotationFS annotationFS : col) {
				Type type = annotationFS.getType();
				String typeShortName = type.getShortName();
				
				if(typeShortName.equals("Dependency"))
				{
					
					Token governor = ((Dependency)annotationFS).getGovernor();
					Token dependent = ((Dependency)annotationFS).getDependent();
					String dependencyType = ((Dependency)annotationFS).getDependencyType();
										
					//tmp
					//System.out.println(governor.getLemma().getValue() + "--" + dependencyType + "--> " + dependent.getLemma().getValue());
					
					String strGovernorId="T"+(governor.getBegin()+Identication)+"S"+(governor.getEnd()+Identication);
					String strDependentId="T"+(dependent.getBegin()+Identication)+"S"+(dependent.getEnd()+Identication);
					
					hashPOS.put(strGovernorId, governor.getPos().getPosValue());
					hashPOS.put(strDependentId, dependent.getPos().getPosValue());
					if(!hashRel.keySet().contains(strGovernorId))
						hashRel.put(strGovernorId, new HashMap<String, String>());
					
					hashRel.get(strGovernorId).put(strDependentId, dependencyType);
					
				}
			}
			
		}

		public static HashMap<String, Boolean> hashTypes = new HashMap<String, Boolean>();
		private static void checkAllTypes(Collection<AnnotationFS> col)
		{
			for (AnnotationFS annotationFS : col) {
				Type type = annotationFS.getType();
				if(!type.toString().startsWith("de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos"))
				{
					String typeShortName = type.getShortName();
					if(!hashTypes.keySet().contains(typeShortName))
						hashTypes.put(typeShortName, true);
				}
				
			}
			
		}
		
		
		private static boolean getIfThereIsDependency(Collection<AnnotationFS> col) {
			for (AnnotationFS annotationFS : col) {
				Type type = annotationFS.getType();
				String typeShortName = type.getShortName();
				if(typeShortName.equals("Dependency"))
					return true;
			}
			return false;
		}

		public static void main(String[] args) throws VisualizerGenerationException {
			
			JCas jCas;
			try {
				jCas = UimaUtils.loadXmi(new File(args[0]));
				
				
				Vector<FeatureValue> featureVector = new Vector<FeatureValue>();
				featureVector.add(new FeatureValue("feature1",0.1));
				featureVector.add(new FeatureValue("feature2",0.3));
				featureVector.add(new FeatureValue("feature3",0.7));
				TEDecisionWithAlignment decision = new                    
		                             TEDecisionWithAlignment(DecisionLabel.Entailment, 0.5, "", jCas, featureVector);
				
				//Set<String> drawnAnnotations = new Set<String>();
				Visualizer vis = new P1EdaVisualizer();
				String str = vis.generateHTML(decision);
				//String str = vis.generateHTML(jCas);
				
				try {
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						    new FileOutputStream("temp.html")));
					bw.write(str);
					bw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				File file = new File("temp.html");
				
				Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			        try {
			            desktop.browse(new URL("file:\\\\\\" + file.getAbsolutePath()).toURI());
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			    } 
				
			} catch (UimaUtilsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		protected void init() {
			strDocText = null;
			strDocData = null;
			strDocEntities = null;

			hashPOS = new HashMap<String, String>();
			hashRel = new HashMap<String, HashMap<String, String>>();
			hashTEEntities = new HashMap<String, String>();
			
			strDocEntities = "var collData = { entity_types: [ \r\n";
			
			docAlignmentData = "docData['alignment_entity'] = [ \r\n";
			strRelationEntities = "collData['relation_types'] = [ \r\n";
			hashAlignmentData = new HashMap<String, String>();
			hashAlignmentEntities = new HashMap<String, String>();
			strRelationData = "docData['relations'] = [ \r\n";
			strHtml = null;
		}

	}
