package eu.excitementproject.eop.globalgraphoptimizer.edgelearners;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.eop.globalgraphoptimizer.alg.TopologicalSorter;
import eu.excitementproject.eop.globalgraphoptimizer.defs.Constants;
import eu.excitementproject.eop.globalgraphoptimizer.defs.Pair;
import eu.excitementproject.eop.globalgraphoptimizer.graph.AbstractOntologyGraph;
import eu.excitementproject.eop.globalgraphoptimizer.graph.AbstractRuleEdge;
import eu.excitementproject.eop.globalgraphoptimizer.graph.DirectedOneMappingOntologyGraph;
import eu.excitementproject.eop.globalgraphoptimizer.graph.NodeGraph;
import eu.excitementproject.eop.globalgraphoptimizer.graph.RelationNode;
import eu.excitementproject.eop.globalgraphoptimizer.graph.RuleEdge;
import eu.excitementproject.eop.globalgraphoptimizer.score.LocalScoreModel;
import eu.excitementproject.eop.globalgraphoptimizer.score.MapLocalScorer;
import eu.excitementproject.eop.globalgraphoptimizer.score.ScoreModel;

/**
 * An implementation of the tree-node-fix algorithm (http://www.cs.tau.ac.il/~jonatha6/homepage_files/publications/ACL12.pdf)
 * with empty graph initialization
 * 
 * @author Jonathan Berant
 *
 */
public class EfficientlyCorrectEmptyTreeLearner implements EdgeLearner {
	
	double m_edgeCost;
	NodeGraph m_nodeGraph;
	ScoreModel m_localModel;
	
	private Logger logger = Logger.getLogger(EfficientlyCorrectEmptyTreeLearner.class);
	
	public EfficientlyCorrectEmptyTreeLearner(NodeGraph ioGraph,LocalScoreModel iLocalModel, double edgeCost) throws Exception {
		m_edgeCost = edgeCost;
		m_nodeGraph = ioGraph;
		m_localModel = iLocalModel;
	}
	
	public EfficientlyCorrectEmptyTreeLearner(double edgeCost) {
		m_nodeGraph = null;
		m_localModel = null;
		m_edgeCost = edgeCost;
	}
	
	public void init(NodeGraph ioGraph,MapLocalScorer iLocalModel) {		
		m_nodeGraph = ioGraph;
		m_localModel = iLocalModel;
	}
	
	/**
	 * We assume here that the entailing edges do not violate the FRG or transitivity properties
	 * @throws Exception
	 */
	public void insertEntailingEdges() throws Exception {
		
		Set<Pair<String,String>> entailing =  m_localModel.getEntailing();
		for(RelationNode n1: m_nodeGraph.getGraph().getNodes()) {
			for(RelationNode n2: m_nodeGraph.getGraph().getNodes()) {
				if(n1!=n2) {
					Pair<String,String> currPair = new Pair<String, String>(n1.description(),n2.description());
					if(entailing.contains(currPair)) {
						m_nodeGraph.getGraph().addEdge(
								new RuleEdge(n1,n2,m_localModel.getEntailmentScore(n1, n2)));
					}
				}
			}
		}	
	}
	
	
	public void learn() throws Exception {
		
		insertEntailingEdges();
		//perform iterations
		performIterativeProcedure();
	}

	protected void performIterativeProcedure() throws Exception {

		AbstractOntologyGraph graph = m_nodeGraph.getGraph();

		//keep going while did there are changes to the graph 
		boolean converge = false;
		double currentObjValue = m_nodeGraph.getGraph().sumOfEdgeWeights()-m_edgeCost*m_nodeGraph.getGraph().getEdgeCount();
		logger.warn("OBJECTIVE-FUNCTION-VALUE: " + currentObjValue);	
		while(!converge) {
			
			for(Integer currNodeId: graph.getNodeIds()) {
				RelationNode currNode = graph.getNode(currNodeId);
				logger.warn("Current node: " + currNode.id());
				reattachNode(currNode);
			}
			double objectiveValue = m_nodeGraph.getGraph().sumOfEdgeWeights()-m_edgeCost*m_nodeGraph.getGraph().getEdgeCount();
			if(objectiveValue+0.00001<currentObjValue)
				throw new LearningException("objective function value can not decrease. Current value: " + currentObjValue + " new value: " + objectiveValue);
			else if(objectiveValue-currentObjValue < Constants.CONVERGENCE)
				converge = true;
			currentObjValue = objectiveValue;
			logger.warn("OBJECTIVE-FUNCTION-VALUE: " + currentObjValue);	
		}
	}

	private void reattachNode(RelationNode removedNode) throws Exception {

		//remove node from graph
		DirectedOneMappingOntologyGraph graph = (DirectedOneMappingOntologyGraph) m_nodeGraph.getGraph();
		graph.removeNode(removedNode.id());
		//generate reduced transitive graph
		DirectedOneMappingOntologyGraph graphCopy = ((DirectedOneMappingOntologyGraph) graph).reduceGraph();
		//get the nodes of the rtf topologically sorted
		List<RelationNode> sortedList = TopologicalSorter.sort(graphCopy);
		//compute S_in and S_out with dynamic programming
		Map<RelationNode,Double> sIn = computeSIn(sortedList,graphCopy,removedNode);
		Map<RelationNode,Double> sOut = computeSOut(sortedList,graphCopy,removedNode);
		//go over all options and find the sin and sout
		OptimalReattachment optReattachment = findOptimalReattachment(graphCopy, sIn, sOut, removedNode, sortedList);
		//reattach node
		graph.addNode(removedNode);
		if(optReattachment.getScore()>0) {

			Set<AbstractRuleEdge> edgesToAdd = new HashSet<AbstractRuleEdge>();
			//add in edges
			if(optReattachment.getOut()!=null) {
				String representOutId = optReattachment.getOut().description().split(",")[0];
				RelationNode representOutNode = graph.getNode(Integer.parseInt(representOutId));
				edgesToAdd.add(new RuleEdge(removedNode,representOutNode,m_localModel.getEntailmentScore(removedNode, representOutNode)));
				for(AbstractRuleEdge outEdge: representOutNode.outEdges())
					edgesToAdd.add(new RuleEdge(removedNode,outEdge.to(),m_localModel.getEntailmentScore(removedNode, outEdge.to())));
			}

			for(RelationNode inNode: optReattachment.getIns()) {

				String representInId = inNode.description().split(",")[0];
				RelationNode representInNode = graph.getNode(Integer.parseInt(representInId));
				edgesToAdd.add(new RuleEdge(representInNode,removedNode,m_localModel.getEntailmentScore(representInNode, removedNode)));
				for(AbstractRuleEdge inEdge: representInNode.inEdges())
					edgesToAdd.add(new RuleEdge(inEdge.from(),removedNode,m_localModel.getEntailmentScore(inEdge.from(), removedNode)));
			}
			for(AbstractRuleEdge edgeToAdd: edgesToAdd)
				graph.addEdge(edgeToAdd);
		}
/* This part is to make sure the algorithm is correct but is not efficient
		if(graph.findViolatedTransitivityConstraints().size() > 0 ||
				graph.findViolatedTreeConstraints().size() > 0)
			throw new OntologyException("Re-attaching the node resulted in a non-transitive or non-rtf graph");
			*/
	}

	private Map<RelationNode, Double> computeSIn(List<RelationNode> sortedList,
			DirectedOneMappingOntologyGraph graphCopy, RelationNode removedNode) throws Exception {

		Map<RelationNode,Double> result = new HashMap<RelationNode, Double>();

		for(RelationNode currNode: sortedList) {

			double nodeScore = 0;
			//recursive call
			for(AbstractRuleEdge inEdge: currNode.inEdges())
				nodeScore+=result.get(inEdge.from());
			//edges in Node
			String[] nodeIds = currNode.description().split(",");
			for(String nodeId:nodeIds) {
				RelationNode fromNode = m_nodeGraph.getGraph().getNode(Integer.parseInt(nodeId));
				nodeScore+=m_localModel.getEntailmentScore(fromNode,removedNode)-m_edgeCost;
			}
			result.put(currNode, nodeScore);
		}
		return result;
	}

	private Map<RelationNode, Double> computeSOut(List<RelationNode> sortedList, 
			DirectedOneMappingOntologyGraph graphCopy, RelationNode removedNode) throws Exception {

		Map<RelationNode,Double> result = new HashMap<RelationNode, Double>();
		for(int i = sortedList.size()-1; i>=0; --i) {

			RelationNode currNode = sortedList.get(i);
			double nodeScore = 0;

			if(currNode.outEdgesCount()>1)
				throw new LearningException("Node: " + currNode + "has more than one parent in the rtf");
			//recursive call
			for(AbstractRuleEdge outEdge: currNode.outEdges())
				nodeScore+=result.get(outEdge.to());
			//edges in Node
			String[] nodeIds = currNode.description().split(",");
			for(String nodeId:nodeIds) {
				RelationNode toNode = m_nodeGraph.getGraph().getNode(Integer.parseInt(nodeId));
				nodeScore+=m_localModel.getEntailmentScore(removedNode,toNode)-m_edgeCost;
			}
			result.put(currNode, nodeScore);
		}
		return result;
	}

	private OptimalReattachment findOptimalReattachment(DirectedOneMappingOntologyGraph graphCopy, Map<RelationNode,Double> sIn,
			Map<RelationNode,Double> sOut, RelationNode removedNode,
			List<RelationNode> sortedList) {

		double bestScore=0;
		RelationNode bestOut=null;
		List<RelationNode> bestIns =  new ArrayList<RelationNode>();
		//1. inside a connectivity component
		for(RelationNode currRtfNode: graphCopy.getNodes()) {
			double currScore = sIn.get(currRtfNode)+sOut.get(currRtfNode);
			if(currScore > bestScore) {
				bestScore = currScore;
				bestOut=currRtfNode;
				bestIns.clear();
				bestIns.add(currRtfNode);
			}
		}
		//as a child of one of the connectivity components
		for(RelationNode currRtfNode: graphCopy.getNodes()) {
			double currScore = sOut.get(currRtfNode);
			List<RelationNode> currIns =  new ArrayList<RelationNode>();
			for(AbstractRuleEdge edge: currRtfNode.inEdges()) {
				if(sIn.get(edge.from())>0) {
					currScore+=sIn.get(edge.from());
					currIns.add(edge.from());
				}
			}
			if(currScore>bestScore) {
				bestScore = currScore;
				bestOut = currRtfNode;
				bestIns = currIns;
			}	
		}
		//as a new root
		double currScore = 0;
		List<RelationNode> currIns =  new ArrayList<RelationNode>();
		for(int i = sortedList.size()-1; i>=0; --i) {

			RelationNode currRootNode = sortedList.get(i);
			if(currRootNode.outEdgesCount()==0) { 
				if(sIn.get(currRootNode)>0) {
					currScore+=sIn.get(currRootNode);
					currIns.add(currRootNode);
				}
			}
		}
		if(currScore>bestScore) {
			bestScore = currScore;
			bestOut = null;
			bestIns = currIns;
		}
		return new OptimalReattachment(bestScore, bestOut, bestIns);
	}
	
	public double getObjectiveFunctionValue() {
		return m_nodeGraph.getGraph().sumOfEdgeWeights()-m_edgeCost*m_nodeGraph.getGraph().getEdgeCount();
	}

	private class OptimalReattachment {

		public OptimalReattachment(double score, RelationNode out, List<RelationNode> ins) {
			m_score = score;
			m_out = out;
			m_ins=ins;
		}

		public double getScore() {
			return m_score;
		}

		public RelationNode getOut() {
			return m_out;
		}

		public List<RelationNode> getIns() {
			return m_ins;
		}
		private double m_score;
		private RelationNode m_out;
		private List<RelationNode> m_ins;
	}
}
