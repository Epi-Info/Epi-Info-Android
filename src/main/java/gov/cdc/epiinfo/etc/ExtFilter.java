package gov.cdc.epiinfo.etc;

import java.io.File;
import java.io.FilenameFilter;

public class ExtFilter implements FilenameFilter {

	private String ext;
	private String altExt;
	private String prefixToAvoid;

	public ExtFilter(String ext, String prefixToAvoid) {

		this.ext = "." + ext.toLowerCase();
		this.prefixToAvoid = prefixToAvoid;
	}

	public ExtFilter(String ext, String altExt, String prefixToAvoid) {

		this.ext = "." + ext.toLowerCase();
		this.altExt = "." + altExt.toLowerCase();
		this.prefixToAvoid = prefixToAvoid;
	}

	public boolean accept(File dir, String name) {

		if (altExt == null || altExt.length() == 0) {
			if (prefixToAvoid != null && prefixToAvoid.length() > 0) {
				return name.toLowerCase().endsWith(ext) && !name.startsWith(prefixToAvoid);
			} else {
				return name.toLowerCase().endsWith(ext);
			}
		}
		else
		{
			if (prefixToAvoid != null && prefixToAvoid.length() > 0) {
				return (name.toLowerCase().endsWith(ext) || name.toLowerCase().endsWith(altExt)) && !name.startsWith(prefixToAvoid);
			} else {
				return name.toLowerCase().endsWith(ext) || name.toLowerCase().endsWith(altExt);
			}
		}
	}
}
