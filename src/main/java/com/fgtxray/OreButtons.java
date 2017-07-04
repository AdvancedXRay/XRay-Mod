// Use by GuiSettings. There is a hashmap of the "OreName" and this

package com.fgtxray;

import com.fgtxray.reference.OreInfo;

import java.util.ArrayList;

public class OreButtons
{
	public String name; // The name of the ore and the text displayed on this button
	public int id; // Id is the first block in the ore dictionary for this type. It's int( str(id) + str(meta) ). So grass is 2+0 or 20.
	public ArrayList<OreInfo> ores = new ArrayList<>(); // List of ores that this button toggles.

	public OreButtons(String name, int id, OreInfo ores)
	{
		this.name = name;
		this.id = id;
		this.ores.add( ores );
	}
}
