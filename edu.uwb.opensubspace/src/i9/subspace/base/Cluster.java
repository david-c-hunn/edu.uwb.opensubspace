package i9.subspace.base;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Representation des Clusters (Dimensionsmenge, Objektmenge)
 * 
 * 
 */
public class Cluster implements Serializable{

	private static final long serialVersionUID = 1L;

	public boolean[] m_subspace;

	public List<Integer> m_objects;

	public Cluster(boolean[] subspace, List<Integer> objects) {
		m_subspace = subspace;
		m_objects = objects;
	}

	public boolean equals(Object obj) {
		Cluster cluster = (Cluster) obj;
		if (m_subspace.length != cluster.m_subspace.length)
			return false;
		for (int i = 0; i < m_subspace.length; i++)
			if (m_subspace[i] != cluster.m_subspace[i])
				return false;
		Collections.sort(m_objects);
		Collections.sort(cluster.m_objects);
		if (!this.m_objects.equals(cluster.m_objects))
			return false;
		return true;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (boolean value : m_subspace)
			buf.append((value) ? "1 " : "0 ");
		// buf.append(":")
		buf.append(m_objects.size() + " ");
		for (int value : m_objects)
			buf.append(value + " ");
		buf.append("\n");
		return buf.toString();
	}

	public String toString2() {
		StringBuffer buf = new StringBuffer();
		for (boolean value : m_subspace)
			buf.append((value) ? "1 " : "0 ");
		buf.append("#: ").append(m_objects.size());
		buf.append("\n");
		return buf.toString();
	}

	public String export() {
		StringBuffer buf = new StringBuffer();
		for (boolean value : m_subspace)
			buf.append((value) ? "true " : "false ");
		for (int value : m_objects)
			buf.append(value + " ");
		buf.append("\n");
		return buf.toString();
	}

	public String toString3() {
		StringBuffer buf = new StringBuffer();
		for (boolean value : m_subspace)
			buf.append((value) ? "1 " : "0 ");
		buf.append("#: ").append(m_objects.size()+" / ");
		for(int i=0;i<m_objects.size();i++)
			buf.append(m_objects.get(i)+" ");
		buf.append("\n");
		return buf.toString();
	}
	
	public String toStringWeka() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (boolean value : m_subspace)
			buf.append((value) ? "1 " : "0 ");
		buf.append("] #"+m_objects.size()+" {");

		for (int value : m_objects)
			buf.append(value + " ");
		
		buf.append("}\n");
		return buf.toString();
	}

}
