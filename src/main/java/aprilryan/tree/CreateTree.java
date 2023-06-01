package aprilryan.tree;

import aprilryan.tree.entity.Tree;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CreateTree {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();

        try {
            manager.getTransaction().begin();

            System.out.println("Введите ID категории: ");
            Long existingTreeId = Long.parseLong(bufferedReader.readLine());

            Tree newTree = new Tree();
            System.out.println("Введите название новой иерархии: ");
            String newTreeName = bufferedReader.readLine();
            newTree.setName(newTreeName);

            if (existingTreeId == 0) {
                newTree.setLevel(0);
                TypedQuery<Integer> maxRightKeyTypedQuery = manager.createQuery(
                        "select max(t.rightKey) from Tree t", Integer.class
                );
                List<Integer> maxRightKeyList = maxRightKeyTypedQuery.getResultList();
                newTree.setLeftKey(maxRightKeyList.get(0) + 1);
                newTree.setRightKey(maxRightKeyList.get(0) + 2);
            } else {
                Tree existingTree = manager.find(Tree.class, existingTreeId);

                newTree.setLeftKey(existingTree.getRightKey());
                newTree.setRightKey(existingTree.getRightKey() + 1);
                newTree.setLevel(existingTree.getLevel() + 1);

                Query treeLeftKeyQuery = manager.createQuery(
                        "update Tree t set t.leftKey = t.leftKey + 2 where t.leftKey > ?1"
                );
                treeLeftKeyQuery.setParameter(1, newTree.getLeftKey());
                treeLeftKeyQuery.executeUpdate();

                Query treeRightKeyQuery = manager.createQuery(
                        "update Tree t set t.rightKey = t.rightKey + 2 where t.rightKey >= ?1"
                );
                treeRightKeyQuery.setParameter(1, existingTree.getRightKey());
                treeRightKeyQuery.executeUpdate();
            }

            manager.persist(newTree);

            manager.getTransaction().commit();
        } catch (IOException e) {
            System.out.println("I/O error");
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}
