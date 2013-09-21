package fgtXray;

import java.util.ArrayList;

public class OreButtons {
	public String name;
	public int id;
	public ArrayList<OreInfo> ores = new ArrayList();
	
	public OreButtons(String name, int id, OreInfo ores){
		this.name = name;
		this.id = id;
		this.ores.add( ores );
	}
	
}
