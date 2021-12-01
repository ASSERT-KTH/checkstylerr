package org.genxdm.processor.io.adapters;

import java.io.IOException;
import java.io.Reader;

import javax.xml.namespace.QName;

import org.genxdm.Cursor;
import org.genxdm.NodeKind;
import org.genxdm.exceptions.PreCondition;
import org.genxdm.processor.output.XmlEncoder;

/** Consumes a cursor (not idempotent) to produce a character stream conforming
 * to the java.io.Reader interface.
 *
 * @param <N> the node abstraction
 */
public class CursoryReader<N>
    extends Reader
{

    /** Initialize a reader from a cursor
     * 
     * @param cursor the cursor over which the reader operates, initialized
     * to the desired starting position; may not be null.
     */
    public CursoryReader(Cursor<N> cursor)
    {
        this.cursor = PreCondition.assertNotNull(cursor, "cursor");
        originId = cursor.getNodeId();
    }

    /** Initialize a reader with synchronized access semantics (per the
     * superclass constructor)
     * 
     * @param cursor the cursor over which the reader operates, initialized
     * to the desired starting position; may not be null.
     * @param lock the object on which to synchronize; may not be null.
     */
    public CursoryReader(Cursor<N> cursor, Object lock)
    {
        super(lock);
        this.cursor = PreCondition.assertNotNull(cursor, "cursor");
        originId = cursor.getNodeId();
    }

    @Override
    public int read(char[] cbuf, int off, int len)
        throws IOException
    {
        if (builder.length() < len)
        {
            lurch();
        }
        int current = builder.length();
        int actual = (current >= len) ? len : current;
        if (actual == 0)
            return -1;
        // actual is the amount to copy (and return)
        builder.getChars(0, actual, cbuf, off);
        builder.delete(0, actual);
        return actual;
    }

    @Override
    public void close()
        throws IOException
    {
        // do nothing
    }
    
    /** Take the next step into the tree.
     * 
     * @throws IOException
     */
    private void lurch()
        throws IOException
    {
        // we know that we haven't processed the current position yet,
        // and that we should be on a child node.
        NodeKind kind = cursor.getNodeKind();
        switch (kind)
        {
            case DOCUMENT : // can only happen on startup
            {
                // nothing here to see; move along to the first child.
                if (cursor.moveToFirstChild())
                    lurch();
                // otherwise, we've still got an empty builder
                break;
            }
            case COMMENT :
            {
                builder.append("<!--" + cursor.getStringValue() + "-->");
                if (cursor.hasNextSibling())
                    cursor.moveToNextSibling();
                else
                    pop();
                break;
            }
            case PROCESSING_INSTRUCTION :
            {
                builder.append("<?" + cursor.getLocalName() + " " + cursor.getStringValue() + "?>");
                if (cursor.hasNextSibling())
                    cursor.moveToNextSibling();
                else
                    pop();
                break;
            }
            case TEXT :
            {
                builder.append(encoder.encodePCData(cursor.getStringValue()));
                if (cursor.hasNextSibling())
                    cursor.moveToNextSibling();
                else
                    pop();
                break;
            }
            case ELEMENT :
            {
                builder.append("<" + getQName(cursor.getPrefix(), cursor.getLocalName()));
                if (cursor.hasNamespaces())
                {
                    for (String prefix : cursor.getNamespaceNames(false))
                        builder.append(" xmlns" + (prefix.equals("") ? "" : ":" + prefix) + "=\"" + encoder.encodeCData(cursor.getNamespaceForPrefix(prefix)) + "\"");
                }
                if (cursor.hasAttributes())
                {
                    for (QName attribute : cursor.getAttributeNames(false))
                        builder.append(" " + getQName(attribute.getPrefix(), attribute.getLocalPart()) + "=\"" + encoder.encodeCData(cursor.getAttributeStringValue(attribute.getNamespaceURI(), attribute.getLocalPart())) + "\"");
                }
                boolean moved = false;
                if (!cursor.hasChildren())
                    builder.append("/>");
                else
                {
                    builder.append(">");
                    moved = cursor.moveToFirstChild();
                }
                if (!moved)
                {
                    if (cursor.hasNextSibling())
                        cursor.moveToNextSibling();
                    else
                        pop();
                }
                break;
            }
            // TODO: wait, maybe these two can be legal? just very short ...
            case NAMESPACE : // can only happen on startup; broken
            case ATTRIBUTE : // can only happen on startup; broken
            default :
            {
                throw new IOException("Invalid state");
            }
        }
    }
    
    /** Step up, insuring that we don't step backwards.
     * 
     */
    private void pop()
    {
        // we are inside a child node. there is no next sibling.
        // we want to move up to the parent, and write its close tag.
        // then we have to find the next thing to move to.
        cursor.moveToParent();
        if (cursor.getNodeKind() == NodeKind.DOCUMENT)
            return; // there's no more pop possible; we're done
        Object currentId = cursor.getNodeId();
        builder.append("</" + getQName(cursor.getPrefix(), cursor.getLocalName()) + ">");
        if (currentId.equals(originId))
            return; // we're back to where we started; *don't* continue.
        if (cursor.hasNextSibling())
            cursor.moveToNextSibling();
        else
            pop();
        
    }

    private String getQName(String prefix, String localName)
    {
        if ( (prefix != null) && !prefix.equals("") )
            return prefix + ":" + localName;
        return localName;
    }

    private final Cursor<N> cursor;
    private final Object originId;
    private final StringBuilder builder = new StringBuilder();
    private final XmlEncoder encoder = new XmlEncoder();
}
