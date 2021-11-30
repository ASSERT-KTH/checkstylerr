

import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;



public class App {



	/*
    * Guava (Google Common Libraries) Base
    * */
    private static class TreeNode {

        int value;

        Set<TreeNode> children;

        TreeNode(int value, TreeNode... children) {
            this.value = value;
            this.children = Sets.newHashSet(children);
        }
    }


    private static class CountingTask extends RecursiveTask<Integer> {

        private final TreeNode node;

        public CountingTask(TreeNode node) {
            this.node = node;
        }

        @Override
        protected Integer compute() {
            return node.value + node.children.stream()
                    .map(childNode -> new CountingTask(childNode).fork())
                    .collect(Collectors.summingInt(ForkJoinTask::join));
        }
    }

    public static void main(String[] args) {

        System.out.println("Hello, Berlin");

        TreeNode tree = new TreeNode(5,
                new TreeNode(3), new TreeNode(2,
                new TreeNode(2), new TreeNode(8)));

        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        int sum = forkJoinPool.invoke(new CountingTask(tree));
    }
}