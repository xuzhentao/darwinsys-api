import java.awt.*;
import java.util.*;

/** 
 * TabLayout, a TabLayout Layout Manager.
 * 
 * <P>Not completely implemented.
 * XXX TODO Handle Insets in case we're in a Frame
 *
 * <P>As with CardLayout, you <I>must</I> use the form add(String, Component);
 *
 * @author Ian Darwin, ian@darwinsys.com, 
 * <A HREF="http://www.darwinsys.com">http://www.darwinsys.com</A>
 */
public class TabLayout implements LayoutManager {
	/** The number of Components currently added */
	protected int nComponents;

	/** Hashtable used to track Components added by named add form;
	 * key is name, value is Panel (or other Component being managed)
	 */
	protected Hashtable curComps;

	/** The "internal Panel", or container for the TabButtons;
	 * user must construct it and give it to us in our constructor. 
	 * We add it to the panel, you just need to construct it.
	 * XXX TODO can we not Construct it, now that we Add it?? 
	 */
	protected Panel p;

	/** The layout manager of the internal Panel */
	protected LayoutManager pl;

	/** The height of the internal Panel */
	protected int topBreak;

	/** The currently-visible component, or null */
	protected Component showing;

	/** Construct a TabLayout given a top Panel and number of rows
	 * Use this form if you know there'll be lots of TabButtons
	 * XXX TODO compute nrows yourself.
	 */
	TabLayout(Panel p, int rows) {
		super();
		nComponents = 0;
		this.p = p;
		// p = new Panel();
		p.setLayout(pl = new GridLayout(rows, 0));
		curComps = new Hashtable();
		showing = null;
	}
	/** Construct and TabLayout only a top Panel */
	TabLayout(Panel p) {
		this(p, 1);
	}

	/** Show one panel, given its name. */
	public void show(String s) {
		Component c = (Component)curComps.get(s);
		System.out.println("Showing " + s + "=" + c);
		if (showing != null)
			showing.setVisible(false);
		c.setVisible(true);
		showing = c;
	}

	/**
	 * Called by AWT when the user uses the form add(name, Component).
	 * Adds the specified component with the specified name to the layout. 
	 *
	 * @param	name	String with location for component c
	 * @param	c	Component to be added.
	 */
	public void addLayoutComponent(String name, Component c) {
		System.out.println("Adding: Name " + name);
		// Add a TabButton to the top Panel.
		// TabButton needs "this" to callback our show().
		p.add(new TabButton(name, this));
		// Add the Component into our Hashtable for later use
		curComps.put(name, c);
		// Hide the Component until a show() call.
		c.setVisible(false);	// initially none showing
	}

	private boolean added = false;
	/**
	 * Called by AWT to lay out the components 
	 * in the target Container at its current size.
	 *
	 * @param	target	Container whose components are to be laid out.
	 */
	public void layoutContainer(Container target) {
		if (!added) {
			target.add(p);
			added = true;
		}
		System.out.println("layoutContainer: size " + target.getSize());
		pl.layoutContainer(p);
System.out.println("Back from pl.layoutContainer");
		Dimension pReq = p.getPreferredSize();
System.out.println("pReq=" + pReq);
		p.setBounds(new Rectangle(0, 0, pReq.width, pReq.height));
		topBreak = p.getSize().height;
System.out.println("TopBreak=" + topBreak);
		Enumeration keys = curComps.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			Component tc = (Component)curComps.get(key);
			Dimension d = tc.getPreferredSize();
			int px, py, pw, ph;
			px = 0;
			py = 0;
			pw = d.width;
			ph = d.height;
			// System.out.println("layoutContainer]: move " +
			// 	tc + " to " + px + ", " + py);
			tc.setBounds(new Rectangle(px, topBreak+py, pw, ph));
		}
	}

	/**
	 * Called from AWT to calculate the minimum size dimensions 
	 * for the target panel given the components in it.
	 * But we use our own list of named insertions, not the
	 * list of Components that the container keeps.
	 *
	 * @param	target	Container to calculate for
	 */
	public Dimension minimumLayoutSize(Container target) {
		// Give a minimum minimum size to we're visible if OOPS
		int minw = 40, minh = 10;
		Enumeration keys = curComps.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			Component tc = (Component)curComps.get(key);
			int px, py, pw, ph;
			Dimension d = tc.getMinimumSize();
			minw = Math.max(minw, d.width);
			minh = Math.max(minh, d.height);
			// System.out.println("minLay, minw = " + 
			// 	minw + "; minh = " + minh);
		}
		return new Dimension(minw, topBreak + minh);
	}

	/**
	 * Called by AWT to compute the preferred size for the target panel
	 * given our list of the components that it contains.
	 *
	 * @param	target	Container to calculate for
	 */
	public Dimension preferredLayoutSize(Container target) {
		// Give a minimum preferred size to we're visible if OOPS
		int prefw = 50, prefh = 10;
		Enumeration keys = curComps.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			Component tc = (Component)curComps.get(key);
			Dimension d = tc.getPreferredSize();
			prefw = Math.max(prefw, d.width);
			prefh = Math.max(prefh, d.height);
			System.out.println(key + " prefLay, prefw = " + 
				prefw + "; prefh = " + prefh);
		}
		if (!added) {
			target.add(p);
			added = true;
		}
		pl.layoutContainer(p);
		Dimension X = p.getPreferredSize();
		prefw = Math.max(prefw, X.width);
		return new Dimension(prefw, topBreak + prefh);
	}

	/**
	 * Called by AWT to remove a given component from the layout. 
	 *
	 * <P>Penalty time: we can only remove by key, so we need to
	 * search the hashtable to find the key for this value.
	 * Since most TabLayouts have only 3-10 tabs, the overhead
	 * of this search won't kill us :-)
	 *
	 * @param	c	Component to be removed
	 */
	public void  removeLayoutComponent(Component c) {
		Enumeration keys = curComps.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			Component val = (Component)curComps.get(key);
			if (val == c) {
				curComps.remove(key);
				break;
			}
		}
	}
}