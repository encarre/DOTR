package algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import base.Settings;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public abstract class AStar {
	public AStar() {
	}
	
	public static class NodeComparator implements Comparator<Node>	{
	    @Override
	    public int compare(Node a, Node b) {
	    	return Double.compare(a.getTotalCost(), b.getTotalCost());
	    }
	}

	private static ArrayList<Node> reconstructPath( Node current) {
		ArrayList<Node> totalPath = new ArrayList<>();
		totalPath.add(current);

		while((current = current.getFatherNode()) != null) {
			totalPath.add(current);
		}

		return totalPath;
	}

	public static ArrayList<Node> shortestPath(Node start, Node end, Pane root, boolean allowDiagonals) {
		Node currentNode = null;
		boolean containsNeighbor;
		
		int cellCount = Settings.gridCellsCountX / Settings.cellSize * Settings.gridCellsCountY / Settings.cellSize;
		Set<Node> closedList = new HashSet<>(cellCount);

		PriorityQueue<Node> openList = new PriorityQueue<>(cellCount , new NodeComparator());
		openList.add(start);
		
		start.setCost(0);
		start.setTotalCost(start.getCost() + heuristicCostEstimate(start,end));
		
		while(!openList.isEmpty()) {
			if(closedList.size() > cellCount) {
				System.out.println("Error infinite loop");
				return null;
			}
			
			currentNode  = openList.poll();
			if(currentNode.getPosition().getX() == end.getPosition().getX() && currentNode.getPosition().getY() == end.getPosition().getY()) {
				ArrayList<Node> reconstructedPath = reconstructPath(currentNode);
				for(int i = 0; i < reconstructedPath.size() - 1; i++) {
					Point2D semiStart = new Point2D(reconstructedPath.get(i).getPosition().getX(), reconstructedPath.get(i).getPosition().getY());
					Point2D semiEnd = new Point2D(reconstructedPath.get(i+1).getPosition().getX(), reconstructedPath.get(i+1).getPosition().getY());
					Line line = new Line();
					line.setStartX(semiStart.getX());
					line.setStartY(semiStart.getY());
					line.setEndX(semiEnd.getX());
					line.setEndY(semiEnd.getY());
					root.getChildren().add(line);
				}

				return reconstructedPath;
			}
			
			closedList.add(currentNode);
			
			for(Node neighbor : currentNode.getNeighbours(allowDiagonals)) {
				double x = neighbor.getPosition().getX();
				double y = neighbor.getPosition().getY();
				
				boolean alreadyVisited = false;
				
				for(Node node : closedList) {
					if(node.getPosition().getX() == x && node.getPosition().getY() == y) {
						alreadyVisited = true;
						break;
					}
				}
						
				if(alreadyVisited){
					continue;
				}
				
				double tentativeScoreG = currentNode.getCost() + 1;
				
				containsNeighbor = false;
				for(Node node : openList) {
					if(node.getPosition().getX() == x && node.getPosition().getY() == y) {
						containsNeighbor = true;
						break;
					}
				}
				
				if(!containsNeighbor || Double.compare(tentativeScoreG, neighbor.getCost()) < 0) {
					neighbor.setFatherNode(currentNode);
					neighbor.setCost(tentativeScoreG);
					neighbor.setHeuristicCost(heuristicCostEstimate(neighbor, end));
					neighbor.setTotalCost(neighbor.getCost() + neighbor.getHeuristicCost());
				}
				
				if(!containsNeighbor) {
					openList.add(neighbor);
				}
			}
		}
		System.out.println("No path");
		return null;
	}

	/**
	 * Distance between two cells. We use the euclidian distance here. 
	 * Used in the algorithm as distance calculation between a cell and the goal. 
	 */
	private static double heuristicCostEstimate(Node start, Node end) {
		final int heuristicWeight = 6;

		Point2D p1 = new Point2D(start.getPosition().getX() , start.getPosition().getY());
		Point2D p2 = new Point2D(end.getPosition().getX() , end.getPosition().getY());
		
		return p1.distance(p2) / Settings.castleSize * heuristicWeight;
	}
}