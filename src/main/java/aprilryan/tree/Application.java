package aprilryan.tree;

import aprilryan.tree.entity.Tree;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Application {

    private static final BufferedReader bufferedReaderIn = new BufferedReader(new InputStreamReader(System.in));

    private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");

    private static final EntityManager manager = factory.createEntityManager();

    public static void main(String[] args) {
        try {
            for (; ; ) {
                System.out.println("- Создание [1]");
                System.out.println("- Перемещение [2]");
                System.out.println("- Удаление [3]");
                System.out.println("- Завершить программу [4]");
                System.out.println("Выберите действие: ");

                int userChoice = Integer.parseInt(bufferedReaderIn.readLine());

                if (userChoice == 1) {
                    create();
                } else if (userChoice == 2) {
                    relocate();
                } else if (userChoice == 3) {
                    delete();
                } else if (userChoice == 4) {
                    break;
                } else {
                    System.out.println("Такого действия нет");
                }
            }
        } catch (IOException e) {
            System.out.println("I/O error");
        }
    }

    private static void create() {
        try {
            manager.getTransaction().begin();

            System.out.println("Введите ID категории: ");
            Long existingTreeId = Long.parseLong(bufferedReaderIn.readLine());

            Tree newTree = new Tree();
            System.out.println("Введите название новой иерархии: ");
            String newTreeName = bufferedReaderIn.readLine();
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

    private static void relocate() {
        try {
            manager.getTransaction().begin();

            System.out.println("Введите ID категории, которую необходимо переместить: ");
            Long movableTreeId = Long.parseLong(bufferedReaderIn.readLine());
            Tree movableTree = manager.find(Tree.class, movableTreeId);

            Query changeKeysToNegative = manager.createQuery(
                    "update Tree t set t.leftKey = t.leftKey * (-1)," +
                            "t.rightKey = t.rightKey * (-1) where t.leftKey between ?1 and ?2"
            );
            changeKeysToNegative.setParameter(1, movableTree.getLeftKey());
            changeKeysToNegative.setParameter(2, movableTree.getRightKey());
            changeKeysToNegative.executeUpdate();

            Query removeSpacesForLeftKeys = manager.createQuery(
                    "update Tree t set t.leftKey = t.leftKey - (?2 - ?1 + 1) where t.leftKey > ?2"
            );
            removeSpacesForLeftKeys.setParameter(1, movableTree.getLeftKey());
            removeSpacesForLeftKeys.setParameter(2, movableTree.getRightKey());
            removeSpacesForLeftKeys.executeUpdate();

            Query removeSpacesForRightKeys = manager.createQuery(
                    "update Tree t set t.rightKey = t.rightKey - (?2 - ?1 + 1) where t.rightKey > ?2"
            );
            removeSpacesForRightKeys.setParameter(1, movableTree.getLeftKey());
            removeSpacesForRightKeys.setParameter(2, movableTree.getRightKey());
            removeSpacesForRightKeys.executeUpdate();

            System.out.println("Введите ID категории, куда необходимо переместить: ");
            Long newHostTreeId = Long.parseLong(bufferedReaderIn.readLine());

            if (newHostTreeId == 0) {
                TypedQuery<Integer> maxRightKeyTypedQuery = manager.createQuery(
                        "select max(t.rightKey) from Tree t", Integer.class
                );
                List<Integer> maxRightKeyList = maxRightKeyTypedQuery.getResultList();

                Query changeKeysAndLevels = manager.createQuery(
                        "update Tree t set t.leftKey = 0 - t.leftKey - ?1 + ?2 + 1, " +
                                "t.rightKey = 0 - t.rightKey - ?1 + ?2 + 1," +
                                "t.level = t.level - ?3 where t.leftKey < 0"
                );
                changeKeysAndLevels.setParameter(1, movableTree.getLeftKey());
                changeKeysAndLevels.setParameter(2, maxRightKeyList.get(0));
                changeKeysAndLevels.setParameter(3, movableTree.getLevel());
                changeKeysAndLevels.executeUpdate();
            } else {
                Tree newHostTree = manager.find(Tree.class, newHostTreeId);

                Query allocateSpaceToLeftKeys = manager.createQuery(
                        "update Tree t set t.leftKey = t.leftKey + (?1 - ?2 + 1) where t.leftKey > ?3"
                );
                allocateSpaceToLeftKeys.setParameter(1, movableTree.getRightKey());
                allocateSpaceToLeftKeys.setParameter(2, movableTree.getLeftKey());
                allocateSpaceToLeftKeys.setParameter(3, newHostTree.getRightKey());
                allocateSpaceToLeftKeys.executeUpdate();

                Query allocateSpaceToRightKeys = manager.createQuery(
                        "update Tree t set t.rightKey = t.rightKey + (?1 - ?2 + 1) where t.rightKey >= ?3"
                );
                allocateSpaceToRightKeys.setParameter(1, movableTree.getRightKey());
                allocateSpaceToRightKeys.setParameter(2, movableTree.getLeftKey());
                allocateSpaceToRightKeys.setParameter(3, newHostTree.getRightKey());
                allocateSpaceToRightKeys.executeUpdate();

                manager.refresh(newHostTree);

                Query relocateKeysAndUpdateLevel = manager.createQuery(
                        "update Tree t set t.leftKey = 0 - t.leftKey + ?1 - ?2 - 1," +
                                "t.rightKey = 0 - t.rightKey + ?1 - ?2 - 1," +
                                "t.level = abs(t.level - ?4) + ?3 + 1 where t.leftKey < 0"
                );
                relocateKeysAndUpdateLevel.setParameter(1, newHostTree.getRightKey());
                relocateKeysAndUpdateLevel.setParameter(2, movableTree.getRightKey());
                relocateKeysAndUpdateLevel.setParameter(3, newHostTree.getLevel());
                relocateKeysAndUpdateLevel.setParameter(4, movableTree.getLevel());
                relocateKeysAndUpdateLevel.executeUpdate();
            }

            manager.getTransaction().commit();
        } catch (IOException e) {
            System.out.println("I/O error");
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    private static void delete() {
        try {
            manager.getTransaction().begin();

            System.out.println("Введите ID категории для удаления: ");
            Long deleteTree = Long.parseLong(bufferedReaderIn.readLine());

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
