package org.wit.rpt.graphplan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.wit.rpt.model.Capability;
import org.wit.rpt.model.CapabilityModel;
import org.wit.rpt.model.Commitment;
import org.wit.rpt.model.ContextState;
import org.wit.rpt.model.ContextStateModel;
import org.wit.rpt.testdrawtool.Proba;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

/**
 * Use capabilities and commitments in graph planning.
 * 
 * @author Guo Jingzhi
 */
public class GraphPlanNotSimCaCo {
	private static ArrayList<ContextState> initialstates;
	private static ArrayList<ContextState> goalstates;
	private static ArrayList<ContextState> allini;
	private static ArrayList<Capability> allcap;
	private static ArrayList<Commitment> allco;
	static String fpcc = "file:D://GCCmodel//CCmodel.owl";
	static String fpccoo = "file:D://GCCmodel//CCmodel1.owl";
	static String fpcco = "D:\\GCCmodel\\CCmodel1.owl";
	static String fpg = "file:D://GCCmodel//goalmodel.owl";
	static String fpd = "file:D://GCCmodel//Domain.owl";
	static ContextStateModel csmi;

	/**
	 * Test this Class.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String fpcs = "file:D://GCCmodel//contextstates.owl";
		csmi = new ContextStateModel(fpcs);
		initialstates = new ArrayList<ContextState>();
		goalstates = new ArrayList<ContextState>();

		ContextState cs1 = new ContextState(csmi, "cs1");
		ContextState cs7 = new ContextState(csmi, "cs7");
		ContextState cs13 = new ContextState(csmi, "cs13");
		initialstates.add(cs1);
		initialstates.add(cs7);
		initialstates.add(cs13);
		ContextState gcs14 = new ContextState(csmi, "cs14");
		goalstates.add(gcs14);

		allini = initialstates;
		drawGraph(initialstates, goalstates);
	}

	/**
	 * Draw graph.
	 * 
	 * @param initialstates
	 * @param goalstates
	 * @throws IOException
	 */
	public static void drawGraph(ArrayList<ContextState> initialstates, ArrayList<ContextState> goalstates)
			throws IOException {
		File f = new File("D://GH-AGVsModel//co_testfile.gv");
		if (f.exists()) {
			f.delete();
		}
		OutputStream out = new FileOutputStream(f, true);
		out.write(new String("").getBytes());
		out.write("digraph a{".getBytes());
		out.write('\r');
		out.write('\n');
		out.write("rankdir = LR;".getBytes());
		out.write('\r');
		out.write('\n');
		out.write("fontsize=35;".getBytes());
		out.write('\r');
		out.write('\n');
		out.write("encoding=\"UTF-8\";".getBytes());
		out.write('\r');
		out.write('\n');
		out.write("bgcolor=\"white\";".getBytes());
		out.write('\r');
		out.write('\n');
		String s = "size=" + "\"400,400\"" + ";";
		out.write(s.getBytes());
		out.write('\r');
		out.write('\n');
		out.write("compound=true;".getBytes());
		out.write('\r');
		out.write('\n');

		long start = System.currentTimeMillis();
		coGraphPlan(initialstates, goalstates);
		long end = System.currentTimeMillis();
		System.out.println("GP(CaCo)-notSim:");
		System.out.println("Time:" + (end - start) + "   (ms)");

		out.write("}".getBytes());
		out.close();

		Proba p = new Proba();
		p.start2CaCo();
	}

	/**
	 * Graph planning based on initial state.
	 * 
	 * @param states
	 * @param goalstates
	 * @throws IOException
	 */
	public static void coGraphPlan(ArrayList<ContextState> states, ArrayList<ContextState> goalstates)
			throws IOException {
		int isco = isco(allini);
		if (isco == 0) {
			ArrayList<Capability> calist = getNextCapability(states);
			ArrayList<ContextState> inisList = getNextinitialstates(calist, states);
			iniToGoalCa(inisList, calist, goalstates);
		} else {
			ArrayList<Commitment> colist = getNextCommitment(states);
			ArrayList<ContextState> inisList = getComNextinitialstates(colist, states);
			iniToGoalCo(inisList, colist, goalstates);
		}
	}

	// 根据承诺，获得承诺的下一步状态
	/**
	 * According to the commitment, get the next state of commitment.
	 * 
	 * @param colist
	 * @param initialstates
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<ContextState> getComNextinitialstates(ArrayList<Commitment> colist,
			ArrayList<ContextState> initialstates) throws IOException {
		ArrayList<ContextState> outlist = new ArrayList<ContextState>();
		for (int i = 0; i < colist.size(); i++) {
			Commitment co = colist.get(i);
			ArrayList<ContextState> conseList = co.getConsequent();
			for (int j = 0; j < conseList.size(); j++) {
				ContextState conCon = conseList.get(j);
				outlist.add(conCon);
				allini.add(conCon);
				if (!initialstates.contains(conCon)) {
					initialstates.add(conCon);
				}
			}
		}
		return outlist;
	}

	/**
	 * Get the next Capability based on the given initial state set.
	 * 
	 * @param initialstates
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList<Capability> getNextCapability(ArrayList<ContextState> initialstates) throws IOException {
		CapabilityModel cm = new CapabilityModel(fpcc);
		OntClass Capability = cm.getCCModel().getOntClass(CapabilityModel.CapabilityURI);
		ArrayList<Capability> inlist1 = new ArrayList<Capability>();
		for (Iterator p = Capability.listInstances(); p.hasNext();) {
			Individual ii = (Individual) p.next();
			String iin = ii.getLocalName();
			Capability ca = new Capability(cm, iin);
			ArrayList<ContextState> inList = ca.getInConstraints();
			int incsize = 0;
			for (int i = 0; i < inList.size(); i++) {
				ContextState s = inList.get(i);
				if (!initialstates.contains(s)) {
					break;
				} else if (!inlist1.contains(ca)) {
					if (allini.contains(s)) {
						incsize++;
						if (incsize == inList.size()) {
							inlist1.add(ca);
							incsize = 0;
							String[] c = { ca.getLabel() + "[shape = invtriange,color=\"blue\",fontsize=35]",
									s.getName() + "[fontsize=35]", s.getName() + "->" + ca.getLabel() };
							writeToFile(c);
						}
					} else {
						break;
					}
				}
			}
		}
		allcap = inlist1;
		return inlist1;
	}

	/**
	 * Next planning algorithm.[Contains commitment]
	 * 
	 * @param ini
	 * @param co
	 * @param goalstates
	 * @throws IOException
	 */
	public static void iniToGoalCo(ArrayList<ContextState> ini, ArrayList<Commitment> co,
			ArrayList<ContextState> goalstates) throws IOException {
		int isco = isco(ini);
		if (isco == 0) {
			ArrayList<Capability> calist1 = getNextCapability2(ini, co);
			for (int i = 0; i < calist1.size(); i++) {
				for (int j = 0; j < calist1.get(i).getInConstraints().size(); j++) {
					ContextState s = calist1.get(i).getInConstraints().get(j);
					String[] c = { calist1.get(i).getLabel() + "[shape = invtriange,color=\"blue\",fontsize=35]",
							s.getName() + "[fontsize=35]", s.getName() + "->" + calist1.get(i).getLabel() };
					writeToFile(c);
				}
			}
			ArrayList<ContextState> inisList1 = getNextinitialstates(calist1, ini);
			if (!goalstates.containsAll(inisList1)) {
				iniToGoalCa(inisList1, calist1, goalstates);
			}
		} else {
			ArrayList<Commitment> colist = getNextCommitment(ini);
			ArrayList<ContextState> inisList = getComNextinitialstates(colist, ini);
			if (!goalstates.containsAll(inisList)) {
				iniToGoalCo(inisList, colist, goalstates);
			}
		}
	}

	/**
	 * Get next commitment based on states.
	 * 
	 * @param initialstates
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList<Commitment> getNextCommitment(ArrayList<ContextState> initialstates) throws IOException {
		CapabilityModel cm = new CapabilityModel(fpccoo);
		OntModel m = cm.getCCModel();
		OntClass Commitment = cm.getCCModel().getOntClass(CapabilityModel.CommitmentURI);
		ArrayList<Commitment> colist = new ArrayList<Commitment>();
		for (Iterator p = Commitment.listInstances(); p.hasNext();) {
			Individual ii = (Individual) p.next();
			String iin = ii.getLocalName();
			Commitment co = new Commitment(m, iin);
			ArrayList<Capability> credList = co.getCreditor();
			ArrayList<Capability> debtList = co.getDebtor();
			ArrayList<ContextState> coList = co.getConsequent();
			ArrayList<ContextState> anList = co.getAntecedent();
			int t = co.isCommitment(allini, anList);
			if (t == anList.size()) {
				colist.add(co);
				for (int n = 0; n < coList.size(); n++) {
					ContextState com = coList.get(n);
					String[] c = { co.getLabel() + "[shape = invtriange,color=\"red\",fontsize=35]",
							com.getName() + "[fontsize=35]", co.getLabel() + "->" + com.getName() };
					writeToFile(c);
				}
				for (int m1 = 0; m1 < anList.size(); m1++) {
					ContextState anq = anList.get(m1);
					String[] c = { co.getLabel() + "[shape = invtriange,color=\"red\",fontsize=35]",
							anq.getName() + "[fontsize=35]", anq.getName() + "->" + co.getLabel() };
					writeToFile(c);
				}
				for (int i = 0; i < credList.size(); i++) {
					Capability c = credList.get(i);
					allcap.add(c);
				}
				for (int i = 0; i < debtList.size(); i++) {
					Capability c = debtList.get(i);
					allcap.add(c);
				}
			}
		}
		allco = colist;
		return colist;
	}

	/**
	 * Get the OutConstraints for this capability.
	 * 
	 * @param calist
	 * @param initialstates
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<ContextState> getNextinitialstates(ArrayList<Capability> calist,
			ArrayList<ContextState> initialstates) throws IOException {
		ArrayList<ContextState> outlist = new ArrayList<ContextState>();
		for (int i = 0; i < calist.size(); i++) {
			Capability ca = calist.get(i);
			ArrayList<ContextState> outList = ca.getOutConstraints();
			for (int j = 0; j < outList.size(); j++) {
				ContextState outCon = outList.get(j);
				String[] c = { ca.getLabel() + "[shape = invtriange,color=\"blue\",fontsize=35]",
						outCon.getName() + "[fontsize=35]", ca.getLabel() + "->" + outCon.getName() };
				writeToFile(c);
				outlist.add(outCon);
				allini.add(outCon);
				if (!initialstates.contains(outCon)) {
					initialstates.add(outCon);
				}
			}
		}
		return outlist;
	}

	/**
	 * Next planning algorithm.[Not contains commitment]
	 * 
	 * @param ini
	 * @param cap
	 * @param goalstates
	 * @throws IOException
	 */
	public static void iniToGoalCa(ArrayList<ContextState> ini, ArrayList<Capability> cap,
			ArrayList<ContextState> goalstates) throws IOException {
		int isco = isco(allini);
		if (isco == 0) {
			ArrayList<Capability> calist1 = getNextCapability1(ini, cap);
			for (int i = 0; i < calist1.size(); i++) {
				for (int j = 0; j < calist1.get(i).getInConstraints().size(); j++) {
					ContextState s = calist1.get(i).getInConstraints().get(j);
					ini.add(s);
					String[] c = { calist1.get(i).getLabel() + "[shape = invtriange,color=\"blue\",fontsize=35]",
							s.getName() + "[fontsize=35]", s.getName() + "->" + calist1.get(i).getLabel() };
					writeToFile(c);
				}
			}
			ArrayList<ContextState> newini = new ArrayList<ContextState>();
			for (ContextState cd : ini) {
				if (!newini.contains(cd)) {
					newini.add(cd);
				}
			}
			ArrayList<ContextState> inisList1 = getNextinitialstates(calist1, ini);
			ArrayList<ContextState> newinisList1 = new ArrayList<ContextState>();
			for (ContextState cd : inisList1) {
				if (!newinisList1.contains(cd)) {
					newinisList1.add(cd);
				}
			}
			int end = 0;
			for (int i = 0; i < inisList1.size(); i++) {
				ContextState s = inisList1.get(i);
				if (goalstates.contains(s)) {
					end = 1;
					break;
				}
			}
			if (end == 0) {
				iniToGoalCa(inisList1, calist1, goalstates);
			}
		} else {
			ArrayList<Commitment> colist = getNextCommitment(ini);
			ArrayList<ContextState> inisList = getComNextinitialstates(colist, ini);
			if (!goalstates.containsAll(inisList)) {
				iniToGoalCo(inisList, colist, goalstates);
			}
		}
	}

	/**
	 * Get the next Capability based on the current state set.
	 * 
	 * @param initialstates
	 * @param calist
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList<Capability> getNextCapability1(ArrayList<ContextState> initialstates,
			ArrayList<Capability> calist) throws IOException {
		CapabilityModel cm = new CapabilityModel(fpcc);
		OntClass Capability = cm.getCCModel().getOntClass(CapabilityModel.CapabilityURI);
		ArrayList<Capability> inlist1 = new ArrayList<Capability>();
		for (Iterator p = Capability.listInstances(); p.hasNext();) {
			Individual ii = (Individual) p.next();
			String iin = ii.getLocalName();
			Capability ca = new Capability(cm, iin);
			if (!allcap.contains(ca)) {
				ArrayList<ContextState> inList = ca.getInConstraints();
				int incsize = 0;
				for (int i = 0; i < inList.size(); i++) {
					ContextState s = inList.get(i);
					if (!allini.contains(s)) {
						break;
					}
					if (!initialstates.contains(s)) {
						initialstates.add(s);
					}
					if (!inlist1.contains(ca) && !calist.contains(ca)) {
						if (allini.contains(s)) {
							incsize++;
							if (incsize == inList.size()) {
								inlist1.add(ca);
								allcap.add(ca);
								incsize = 0;
							}
						} else {
							break;
						}
					}
				}
			}
		}
		return inlist1;
	}

	/**
	 * Get the next Capability based on the current state set.
	 * 
	 * @param initialstates
	 * @param colist
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList<Capability> getNextCapability2(ArrayList<ContextState> initialstates,
			ArrayList<Commitment> colist) throws IOException {
		CapabilityModel cm = new CapabilityModel(fpcc);
		OntClass Capability = cm.getCCModel().getOntClass(CapabilityModel.CapabilityURI);
		ArrayList<Capability> inlist1 = new ArrayList<Capability>();
		for (Iterator p = Capability.listInstances(); p.hasNext();) {
			Individual ii = (Individual) p.next();
			String iin = ii.getLocalName();
			Capability ca = new Capability(cm, iin);
			if (!allcap.contains(ca)) {
				ArrayList<ContextState> inList = ca.getInConstraints();
				int incsize = 0;
				for (int i = 0; i < inList.size(); i++) {
					ContextState s = inList.get(i);
					if (!allini.contains(s)) {
						break;
					}
					if (!initialstates.contains(s)) {
						initialstates.add(s);
					}
					if (!inlist1.contains(ca) && !colist.contains(ca)) {
						if (allini.contains(s)) {
							incsize++;
							if (incsize == inList.size()) {
								inlist1.add(ca);
								allcap.add(ca);
								incsize = 0;
							}
						} else {
							break;
						}
					}
				}
			}
		}
		return inlist1;
	}

	/**
	 * Determine if there is collaboration.
	 * 
	 * @param initialstates
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static int isco(ArrayList<ContextState> initialstates) throws IOException {
		int isco = 0;
		CapabilityModel cm = new CapabilityModel(fpccoo);
		OntModel m = cm.getCCModel();
		OntClass Commitment = cm.getCCModel().getOntClass(CapabilityModel.CommitmentURI);
		for (Iterator p = Commitment.listInstances(); p.hasNext();) {
			Individual ii = (Individual) p.next();
			String iin = ii.getLocalName();
			Commitment co = new Commitment(m, iin);
			ArrayList<ContextState> anList = co.getAntecedent();
			int t = co.isCommitment(initialstates, anList);
			if (t == anList.size()) {
				if (allco == null) {
					isco = 1;
				} else {
					isco = 0;
				}
			}
		}
		return isco;
	}

	/**
	 * Write the statement into the file.
	 * 
	 * @param s
	 * @throws IOException
	 */
	public static void writeToFile(String s[]) throws IOException {
		File f = new File("D://GH-AGVsModel" + File.separator + "co_testfile.gv");
		OutputStream out = new FileOutputStream(f, true);
		for (int i = 0; i < s.length; i++) {
			out.write(s[i].getBytes());
			out.write(";".getBytes());
			out.write('\r');
			out.write('\n');
		}
		out.close();
	}

}
