String[] args = this.interpreter.get("bsh.args");
if (args == null || args.length != 2) {
	System.err.println("The source and destination files must be specified");
	System.exit(-1);
}

import java.io.*;

InputStream is = new FileInputStream(args[0]);
Reader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
StringWriter sw = new StringWriter();

char[] buf = new char[1024];
int len;
while ((len = in.read(buf, 0, buf.length)) >= 0)
	sw.write(buf, 0, len);
buf = null;
in.close();
is.close();

StringBuffer sb = sw.getBuffer();
char NQ = ' ', quote = NQ;
boolean reqex = false;
len = sb.length();
for (int j = 0, lineno = 1; j < len; j++) {
	if (sb.charAt(j) == '\n')
		++lineno;

	if (quote != NQ) {
		if (sb.charAt(j) == quote)
			quote = NQ;
		else if (sb.charAt(j) == '\\')
			j++;
		else if (sb.charAt(j) == '\n') {
			System.err.println("Unterminated string at line "+lineno);
			System.exit(-1);
		}
	} else if (sb.charAt(j) == '/' && j + 1 < len
	&& (sb.charAt(j + 1) == '*' || sb.charAt(j + 1) == '/')) {
		int l = j;
		boolean eol = sb.charAt(++j) == '/';
		while (++j < len) {
			if (sb.charAt(j) == '\n')
				++lineno;

			if (eol) {
				if (sb.charAt(j) == '\n') {
					sb.delete(l, sb.charAt(j - 1) == '\r' ? j - 1: j);
					len = sb.length();
					j = l;
					break;
				}
			} else if (sb.charAt(j) == '*' && j + 1 < len && sb.charAt(j + 1) == '/') {
				sb.delete(l, j + 2);
				len = sb.length();
				j = l;
				break;
			}
		}
	} else if (sb.charAt(j) == '\'' || sb.charAt(j) == '"') {
		quote = sb.charAt(j);
	} else if (sb.charAt(j) == '/') { //regex
		boolean regex = false;
		for (int k = j;;) {
			if (--k < 0) {
				regex = true;
				break;
			}

			char ck = sb.charAt(k);
			if (!Character.isWhitespace(ck)) {
				regex = ck == '(' || ck == ',' || ck == '=' || ck == ':'
					|| ck == '?' || ck == '{' || ck == '[' || ck == ';'
					|| ck == '!' || ck == '&' || ck == '|' || ck == '^'
					|| (ck == 'n' && k > 4 && "return".equals(sb.substring(k-5, k+1)))
					|| (ck == 'e' && k > 2 && "case".equals(sb.substring(k-3, k+1)));
				break;
			}
		}
		if (regex) {
			while (++j < len && sb.charAt(j) != '/') {
				if (sb.charAt(j) == '\\')
					j++;
				else if (sb.charAt(j) == '\n') {
					System.err.println("Unterminated regex at line "+lineno);
					System.exit(-1);
				}
			}
		}
	}
}

OutputStream os = new FileOutputStream(args[1]);
Writer out = new OutputStreamWriter(os, "UTF-8");
out.write(sb.toString());
out.close();
os.close();
