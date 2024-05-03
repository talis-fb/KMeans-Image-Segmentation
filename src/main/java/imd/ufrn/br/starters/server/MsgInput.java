package imd.ufrn.br.starters.server;

import imd.ufrn.br.entities.Point;

import java.io.Serializable;
import java.util.List;

public class MsgInput implements Serializable {
    public int k;
    public List<Point> initialPoints;
}
