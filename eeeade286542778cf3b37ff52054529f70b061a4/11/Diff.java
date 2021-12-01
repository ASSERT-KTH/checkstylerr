package org.obolibrary.oboformat.diff;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;

import javax.annotation.Nullable;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;

/** Diff. */
public class Diff {

    String type;
    final String frameType;
    @Nullable Frame frame1;
    @Nullable Frame frame2;
    @Nullable Clause clause1;
    @Nullable Clause clause2;
    final int frameNum;

    /**
     * @param ftype
     *        ftype
     * @param type
     *        type
     * @param f1
     *        f1
     * @param n
     *        n
     */
    public Diff(String ftype, String type, Frame f1, int n) {
        this(ftype, type, f1, null, null, n);
    }

    /**
     * @param ftype
     *        ftype
     * @param type
     *        type
     * @param f1
     *        f1
     * @param f2
     *        f2
     * @param c
     *        c
     * @param n
     *        n
     */
    public Diff(String ftype, String type, Frame f1, @Nullable Frame f2, @Nullable Clause c, int n) {
        this.type = type;
        frame1 = f1;
        frame2 = f2;
        clause1 = c;
        frameNum = n;
        frameType = ftype;
    }

    /**
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *        type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return frame1
     */
    @Nullable
    public Frame getFrame1() {
        return frame1;
    }

    /**
     * @param frame1
     *        frame1
     */
    public void setFrame1(Frame frame1) {
        this.frame1 = frame1;
    }

    /**
     * @return frame2
     */
    @Nullable
    public Frame getFrame2() {
        return frame2;
    }

    /**
     * @param frame2
     *        frame2
     */
    public void setFrame2(Frame frame2) {
        this.frame2 = frame2;
    }

    /**
     * @return clause1
     */
    @Nullable
    public Clause getClause1() {
        return clause1;
    }

    /**
     * @param clause1
     *        clause1
     */
    public void setClause1(Clause clause1) {
        this.clause1 = clause1;
    }

    /**
     * @return clause2
     */
    @Nullable
    public Clause getClause2() {
        return clause2;
    }

    /**
     * @param clause2
     *        clause2
     */
    public void setClause2(Clause clause2) {
        this.clause2 = clause2;
    }

    @Override
    public String toString() {
        return type + ' ' + frameType + " Frame1=" + id(frame1) + " Frame2=" + id(frame2) + " Clause1="
            + (clause1 == null ? "-" : clause1) + " Clause2=" + (clause2 == null ? "-" : clause2) + " In=Frame"
            + frameNum;
    }

    @Nullable
    protected String id(@Nullable Frame f) {
        if (f == null) {
            return "-";
        }
        return verifyNotNull(f).getId();
    }
    /*
     * public String toOboDelta() throws IOException { Vector<String> lines =
     * new Vector(); String line1 = null; if(frame1.getType() !=
     * frame2.getType()) { throw new
     * IOException("Frames must be of same type: "+frame1+" -vs- "+frame2); }
     * if(frame1.getType() == FrameType.TERM) line1 = "[Term]"; else
     * if(frame1.getType() == FrameType.TYPEDEF) line1 = "[Typedef]"; else
     * if(frame1.getType() == FrameType.INSTANCE) line1 = "[Instance]";
     * lines.add(line1); lines.add("id: "+frame1.getId()); if (clause1 == null)
     * { } else { lines.add("-"); } return line1 + "\n" + line2 + "\n"; }
     */
}
