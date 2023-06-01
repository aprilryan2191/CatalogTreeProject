package aprilryan.tree;

import aprilryan.tree.entity.Tree;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class TreeMain {

    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();

        try {
            manager.getTransaction().begin();

            TypedQuery<Tree> treeTypedQuery = manager.createQuery(
                    "select t from Tree t", Tree.class
            );
            List<Tree> treeList = treeTypedQuery.getResultList();

            String dash = "-";
            for (Tree tree : treeList) {
                System.out.println(dash.repeat(tree.getLevel()) + tree.getName());
            }

            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}
