package aprilryan.tree;

import aprilryan.tree.entity.Tree;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeleteTree {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();

        try {
            manager.getTransaction().begin();

            System.out.println("Введите ID категории для удаления: ");
            Long deleteTree = Long.parseLong(bufferedReader.readLine());

            Tree existingTree = manager.find(Tree.class, deleteTree);

            Query treeDeleteQuery = manager.createQuery(
                    "delete from Tree t where t.leftKey between ?1 and ?2"
            );
            treeDeleteQuery.setParameter(1, existingTree.getLeftKey());
            treeDeleteQuery.setParameter(2, existingTree.getRightKey());
            treeDeleteQuery.executeUpdate();

            Query treeLeftKeyQuery = manager.createQuery(
                    "update Tree t set t.leftKey = t.leftKey - (?2 - ?1 + 1) where t.leftKey > ?2"
            );
            treeLeftKeyQuery.setParameter(1, existingTree.getLeftKey());
            treeLeftKeyQuery.setParameter(2, existingTree.getRightKey());
            treeLeftKeyQuery.executeUpdate();

            Query treeRightKeyQuery = manager.createQuery(
                    "update Tree t set t.rightKey = t.rightKey - (?2 - ?1 + 1) where t.rightKey > ?2"
            );
            treeRightKeyQuery.setParameter(1, existingTree.getLeftKey());
            treeRightKeyQuery.setParameter(2, existingTree.getRightKey());
            treeRightKeyQuery.executeUpdate();

            manager.getTransaction().commit();
        } catch (IOException e) {
            System.out.println("I/O error");
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}
