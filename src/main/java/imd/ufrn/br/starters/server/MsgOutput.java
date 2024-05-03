package imd.ufrn.br.starters.server;

import imd.ufrn.br.entities.Point;

import java.io.Serializable;
import java.util.List;

public class MsgOutput implements Serializable {
    public List<Point> centroids;
}
