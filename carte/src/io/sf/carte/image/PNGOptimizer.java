/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.image;

import java.io.File;
import java.io.IOException;

/**
 * Optimize a PNG file by spawning an {@code optipng} process.
 * <p>
 * WARNING: this class is at risk of being removed.
 * </p>
 */
public class PNGOptimizer {

	private String pathOptipng;

	public PNGOptimizer() {
		this("optipng");
	}

	public PNGOptimizer(String pathOptipng) {
		super();
		this.pathOptipng = pathOptipng;
	}

	public void optimize(String infile, File outfile, int ncolors) throws ForkException, IOException {
		String[] pcmd;
		pcmd = new String[4];
		pcmd[0] = pathOptipng;
		pcmd[1] = "-q";
		pcmd[2] = "-o7";
		pcmd[3] = infile;
		Process p = Runtime.getRuntime().exec(pcmd);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			throw new ForkException("Interrupted optipng", e);
		}
		int exi = p.exitValue();
		if (exi != 0) {
			throw new ForkException("Bad return code for optipng: " + exi);
		}
		if (ncolors > 0) {
			File f = new File(infile);
			f.renameTo(outfile);
		}
	}

}
