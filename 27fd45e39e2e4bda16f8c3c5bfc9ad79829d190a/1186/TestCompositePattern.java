package design.design_patterns.composite.htmltags;

/*
Composite Pattern
-----------------

The English meaning of the word Composite is something that is made up of complicated and related 
parts. The composite means “putting together” and this is what this design pattern is all about.

There are times when you feel a need of a tree data structure in your code. There are many variations 
to the tree data structure, but sometimes there is a need of a tree in which both branches as well 
as leafs of the tree should be treated as uniformly.

The Composite Pattern allows you to compose objects into a tree structure to represent the part-whole 
hierarchy which means you can create a tree of objects that is made of different parts, but that 
can be treated as a whole one big thing. Composite lets clients to treat individual objects and 
compositions of objects uniformly, that’s the intent of the Composite Pattern.

There can be lots of practical examples of the Composite Pattern. A file directory system, an html 
representation in java, an XML parser all are well managed composites and all can easily be represented 
using the Composite Pattern. But before digging into the details of an example, let’s see some 
more details regarding the Composite Pattern.


The formal definition of the Composite Pattern says that it allows you to compose objects into tree 
structures to represent part-whole hierarchies. Composite lets clients to treat individual objects 
and compositions of objects uniformly.

If you are familiar with a tree data structure, you will know a tree has parents and their children. 
There can be multiple children to a parent, but only one parent per child. In Composite Pattern, 
elements with children are called as Nodes, and elements without children are called as Leafs.

The Composite Pattern allows us to build structures of objects in the form of trees that contains 
both composition of objects and individual objects as nodes. Using a composite structure, we can 
apply the same operations over both composites and individual objects. In other words, in most 
cases we can ignore the differences between compositions of objects and individual objects.



The Composite Pattern has four participants:

    Component

    Leaf
    
    Composite
    
    Client
*/
public class TestCompositePattern {

/*
    <html>
        <body>
            <p>
                Testing html tag library
            </p>
            <p>
                Paragraph 2
            </p>
        </body>
    </html>
*/    


    public static void main(String[] args) {

        // parent elements
        HtmlTag htmlTag = new HtmlParentElement("<html>");
        htmlTag.setStartTag("<html>");
        htmlTag.setEndTag("</html>");

        HtmlTag bodyTag = new HtmlParentElement("<body>");
        bodyTag.setStartTag("<body>");
        bodyTag.setEndTag("</body>");

        htmlTag.addChildTag(bodyTag);

        // insider elements of from the parent
        HtmlTag paragraphTag = new HtmlElement("<p>");
        paragraphTag.setStartTag("<p>");
        paragraphTag.setEndTag("</p>");
        paragraphTag.setTagBody("Testing html tag library");
        bodyTag.addChildTag(paragraphTag);

        paragraphTag = new HtmlElement("<p>");
        paragraphTag.setStartTag("<p>");
        paragraphTag.setEndTag("</p>");
        paragraphTag.setTagBody("Paragraph 2");
        bodyTag.addChildTag(paragraphTag);

        htmlTag.generateHtml();
    }
}


