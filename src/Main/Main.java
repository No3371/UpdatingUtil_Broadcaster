package Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {
	static FileWriter PL;
	static ArrayList<String> patchLog = new ArrayList<String>();
	static ArrayList<String> PpatchLog = new ArrayList<String>();
	static ArrayList<String> oldFile = new ArrayList<String>();
	static ArrayList<String> newFile = new ArrayList<String>();
	static String pDir = "..\\pVer\\";
	static String ppDir = "..\\ppVer\\";
	static String Dir = ".\\"; // Current working directory.
	static File pDirF = new File(pDir);
	static String[] tstr;
	static String Vstr, MVersion, SVersion, Date, patchNoS;
	static int patchNo = 0;
	static boolean updated;

	static JFrame window = new JFrame();
	static JLabel label = new JLabel();

	static FileUtils utils = new FileUtils();
	static FileWriter log;

	static int s;

	public static void window() {
		window.setSize(450, 180);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
		window.add(label);
		window.setVisible(true);
	}

	public static void opt(String strrr) {
		System.out.println(strrr);
		try {
			log.write("  Console: " + strrr + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getPatchNo() {
		patchNo--;
		return String.format("No.%s%s%s%03d", MVersion, SVersion, Date, patchNo);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		File p = new File("FileList");
		p.setWritable(true);

		if (!pDirF.exists())
			pDirF.mkdir();

		log = new FileWriter("log");
		window();
		log.write("�D�禡�A������l��\n");

		backup(ppDir, pDir);
		opt("backup 1 Done");

		tstr = getVersion();

		String str;
		File F = new File(pDir + "FileList");
		if (!F.exists()) {
			label.setText("�ƥ��ɮפ��s�b�C");
			oldFile = null;
			opt("pFileList not found");
		} else {
			FileInputStream IS = new FileInputStream(F);
			BufferedReader BF = new BufferedReader(new InputStreamReader(IS));

			while ((str = BF.readLine()) != null) {
				if (str.startsWith("@u@"))
					str = str.substring(3);
				oldFile.add(str);
			}
			BF.close();
		}

		opt("Starting mainProcess");
		mainProcess(Dir, "FileList");
		opt("mainProcess Done.  --> versionupdate()");

		s = versionupdate();
		opt("VersionUpdate()���\\n");
		switch (s) {
		case 0:
			label.setText("�S����s�C");
			opt("No update.");
			break;
		case 1:
			label.setText("�����C");
			opt("Finish.");
			break;
		case 2:
			break;

		}
		
		if (oldFile != null) {
			for (String s : oldFile) {
				if (!newFile.contains(s)) {
					opt("It seems that the " + s
							+ " in previous version was deleted");
					patchLog.add(" �ɮקR���G " + s);
				}
			}
		}

		backup(pDir, Dir);
		opt("backup 2 done");

		getPatchLog();

		opt("patchlog writing");
		FileWriter plog = new FileWriter("patchlog.txt");
		patchNo = patchLog.size()+1;
		patchNoS = String.format("%s%03d", Date, patchNo - 1);
		for (String a : patchLog) {
			String strt = getPatchNo();
			opt("patchLog write: " + strt + a);
			plog.write(strt + a + "\n");
		}
		for (String a : PpatchLog) {
			opt("(Previous) patchLog write: " + a);
			plog.write(a + "\n");
		}
	

		FileWriter fw = new FileWriter("Version");
		fw.write(String.format("%s %s.%s.%s", Vstr, MVersion, SVersion,
				patchNoS));
		fw.close();
		opt("New Ver: " + Vstr + MVersion + SVersion + patchNoS);

		plog.close();
		opt("patchlog�g�J\n");
		log.close();	
		
		label.setText("����");

		p.setReadOnly();

	}

	public static void backup(String d, String s) throws IOException {
		if (!new File(s).exists())
			return;

		label.setText("�ƥ���");
		File dDirf = new File(d);
		if (dDirf.exists())
			dDirf.delete();
		else
			dDirf.mkdir();
		FileUtils.copyDirectory(new File(s), dDirf);
		label.setText("�ƥ�����");
	}

	public static void mainProcess(String dir, String FLN) throws IOException {
		label.setText("�D�{��......");
		PL = new FileWriter(FLN);
		File d = new File(dir);
		opt("  ::mainProcess:File");
		for (String f : d.list()) {
			if (new File(f).isFile()) {
				if (filter(f)) {
					opt("    ::mainProcess:File: " + f + " skipped");
					continue;
				}
				newFile.add(f);
				if (cmpProcess(f)) {
					opt("    ::" + f + " is a updated File!");
					updated = true;
					PL.write("@u@" + f + "\n");
				} else
					PL.write(f + "\n");
				opt("    ::mainProcess:File: " + f + " CMP pass");
				label.setText("�����B�z" + f);
			}
		}
		for (String f : d.list()) {
			if (new File(f).isDirectory()) {
				opt("Starting dirProcess: " + f + "...");
				dirProcess(dir + f + "\\");
			}
		}
		PL.close();
		return;
	}

	public static void dirProcess(String dir) {
		label.setText("��Ƨ��{��......");
		File d = new File(dir);
		for (String f : d.list()) {
			if (new File(dir + f).isFile()) {
				if (filter(f)) {
					opt("  ::dirProcess:File: " + f + " skipped");
					continue;
				}
				String t = new String(dir + f);
				if (t.startsWith(".\\"))
					t = t.substring(2);
				newFile.add(t);
				try {
					if (cmpProcess(t)) {
						opt("      ::" + f + " is a updated File!");
						updated = true;
						PL.write("@u@" + t + "\n");

					} else
						PL.write(t + "\n");
					label.setText("�����B�z" + t);
					opt("  ::dirProcess:File: " + t + " done");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for (String f : d.list()) {
			if (new File(dir + f).isDirectory()) {
				opt("Starting dirProcess: " + f + "...");
				dirProcess(dir + f + "\\");
			}
		}
		return;
	}

	public static boolean filter(String f) {
		String[] str = { "Lister.jar", "log", "desktop.ini", "patchlog.txt",
				"FileList", "Launch.vbe", "Launcher.jar", "�Ұʾ�.jar", "LauncherCore.jar" };
		for (String s : str) {
			if (f.equals(s)) {
				opt("  ::filter: " + f + " match: " + s);
				return true;
			}
		}
		opt("  ::filter: " + f + " pass");
		return false;
	}

	public static boolean cmpProcess(String fp) throws IOException {
		if (oldFile == null || fp.equals("Version"))
			return false;
		label.setText("���" + fp + "�P" + pDir + fp);
		opt("    ::Comparing: " + fp + " ||| " + pDir + fp);
		if (oldFile.contains(fp)) {
			if (!FileUtils.contentEquals(new File(pDir + fp), new File(fp))) {
				patchLog.add(" �ɮק�s�G " + fp);
				return true;
			}
		} else {
			opt("    ::It's a new file");
			patchLog.add(" �ɮ׷s�W�G " + fp);
			label.setText(pDir + fp + "���s�b�C");
		}
		opt("    ::same");
		return false; // the same named files have nothing different or no same
						// named files.
	}

	public static int versionupdate() {
		opt("Starting versionUpdate()...");

		try {
			if (oldFile != null) {
				File f1 = new File("FileList");
				File f2 = new File(pDir + "FileList");
				if (f2.exists()) {
					opt("Got both fileLists");
					if (FileUtils.contentEquals(f1, f2)) {
						opt("  ::The 2 Filelists are same...");
						if (!updated) {
							opt("  ::And there's no updated file.");
							return 0;
						} else {
							opt("  ::But Some files are updated.");
						}
					}
				}

				SVersion = String.format("%02d",
						(Integer.parseInt(SVersion) + 1));

				Calendar now = Calendar.getInstance();
				Date = String.format("%s%02d%02d",
						Integer.toString(now.get(Calendar.YEAR)).substring(2),
						(now.get(Calendar.MONTH) + 1),
						now.get(Calendar.DAY_OF_MONTH));
				opt("Calendar event done: " + Date);

				return 1;

			} else {
				return 1;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	public static void getPatchLog() {
		opt("reading patchLog file...");

		try {
			File logf = new File("patchlog.txt");
			if (!logf.exists()) {
				opt("patchlog Not Found");
				label.setText("�䤣���s�ǦC�C");
				return;
			}
			opt("Got previous patchlog file");

			FileReader PLog = new FileReader("patchlog.txt");
			BufferedReader br = new BufferedReader(PLog);

			String str;

			while ((str = br.readLine()) != null) {
				PpatchLog.add(str);
				if (PpatchLog.size() >= 500)
					break;
			}

			PLog.close();
			br.close();

			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	public static String[] getVersion() {
		opt("reading version file...");

		try {
			File lver = new File("Version");
			if (!lver.exists()) {
				opt("Version Not Found");
				label.setText("�䤣�쪩����T�C");					
				
				Vstr = "New";
				MVersion = "0";
				SVersion = "00";
				
				return null;
			}
			
			opt("Got Version file");
			String str;
			String[] tstr = new String[5];

			FileReader localVer = new FileReader("Version");
			BufferedReader br = new BufferedReader(localVer);

			if ((str = br.readLine()) != null) {
				str = str.replace(".", " ");
				tstr = str.split("\\s");
			}

			Vstr = tstr[0];
			MVersion = tstr[1];
			SVersion = tstr[2];
			Date = tstr[3].substring(0, 6);
			patchNoS = tstr[3];

			localVer.close();
			opt("Version read: " + Vstr + " " + patchNoS);

			br.close();
			return tstr;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
