
package org.monte.media.tiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;
import javax.swing.tree.DefaultMutableTreeNode;


public abstract class TIFFNode {


    protected TIFFTag tag;

    private ArrayList<TIFFNode> children = new ArrayList<TIFFNode>();
    private TIFFNode parent;

    protected IFDEntry ifdEntry;

    public TIFFNode(TIFFTag tag) {
        this.tag = tag;
    }

    public String getTagName() {
        return tag == null ? "unnamed" : tag.getName();
    }

    public TIFFTag getTag() {
        return tag;
    }


    public int getTagNumber() {
        return tag != null ? tag.getNumber() : -1;
    }

    public IFDEntry getIFDEntry() {
        return ifdEntry;
    }

    public void add(TIFFNode node) {
        children.add(node);
        node.parent = this;
    }

    public ArrayList<TIFFNode> getChildren() {
        return children;
    }

    public TIFFNode getParent() {
        return parent;
    }

    public Iterator<TIFFNode> preorderIterator() {
        return new PreorderIterator(this);
    }

    public Iterator<TIFFNode> postorderIterator() {
        return new PostorderIterator(this);
    }

    public int getChildCount() {
        return children.size();
    }

    public TIFFNode getChildAt(int i) {
        return children.get(i);
    }

    public void removeFromParent() {
        if (parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    private static class PreorderIterator implements Iterator<TIFFNode> {

        private Stack<Iterator<TIFFNode>> stack = new Stack<Iterator<TIFFNode>>();
        private TIFFNode current;

        private PreorderIterator(TIFFNode root) {
            LinkedList ll = new LinkedList<TIFFNode>();
            ll.add(root);
            stack.push(ll.iterator());
        }

        @Override
        public boolean hasNext() {
            return (!stack.empty()
                    && stack.peek().hasNext());
        }

        @Override
        public TIFFNode next() {
            Iterator<TIFFNode> enumer = stack.peek();
            current = enumer.next();
            Iterator<TIFFNode> children = ((ArrayList<TIFFNode>) current.getChildren().clone()).iterator();

            if (!enumer.hasNext()) {
                stack.pop();
            }
            if (children.hasNext()) {
                stack.push(children);
            }
            return current;
        }

        @Override
        public void remove() {
            current.removeFromParent();
        }
    }

    private static class PostorderIterator implements Iterator<TIFFNode> {

        protected TIFFNode root;
        protected Iterator<TIFFNode> children;
        protected Iterator<TIFFNode> subtree;
        private TIFFNode current;

        private PostorderIterator(TIFFNode rootNode) {
            root = rootNode;
            children = ((ArrayList<TIFFNode>) root.children.clone()).iterator();
            subtree = EMPTY_ITERATOR;
        }

        @Override
        public boolean hasNext() {
            return root != null;
        }

        @Override
        public TIFFNode next() {

            if (subtree.hasNext()) {
                current = subtree.next();
            } else if (children.hasNext()) {
                subtree = new PostorderIterator(
                        children.next());
                current = subtree.next();
            } else {
                current = root;
                root = null;
            }

            return current;
        }

        @Override
        public void remove() {
            current.removeFromParent();
        }
    }
    static private final Iterator<TIFFNode> EMPTY_ITERATOR = new Iterator<TIFFNode>() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public TIFFNode next() {
            throw new NoSuchElementException("No more elements");
        }

        @Override
        public void remove() {
            throw new NoSuchElementException("No more elements");
        }
    };
}
