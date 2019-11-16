package lpbcast;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;

public class Custom2DEdge implements EdgeStyleOGL2D{

	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		if(edge.getWeight() == Visualization.EdgeType.RETRIEVE.ordinal()) {
			return 1;
		} else {
			return 3;
		}
	}

	@Override
	public Color getColor(RepastEdge<?> edge) {
		if(edge.getWeight() == Visualization.EdgeType.FANOUT.ordinal()) {
			return Color.green;
		} else if(edge.getWeight() == Visualization.EdgeType.VIEW.ordinal()){
			return Color.gray;
		} else {
			return Color.red;
		}
	}

}
