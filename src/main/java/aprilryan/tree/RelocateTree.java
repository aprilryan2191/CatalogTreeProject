package aprilryan.tree;

import aprilryan.tree.entity.Tree;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class RelocateTree {

    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();

        try {
            manager.getTransaction().begin();

            // 1. Меняем ключи категории, которую мы хотим переместить, на отрицательные значениея (* -1).
            // 2. Убираем появившиеся пробелы.
            // 3. Выделяем места
            // 4. Перемещаем ключи

            System.out.println("Введите ID категории, которую необходимо переместить: ");
            Long movableTreeId = Long.parseLong(bufferedReader.readLine());
            Tree movableTree = manager.find(Tree.class, movableTreeId);

            Query changeLeftToNegative = manager.createQuery(
                    "update Tree t set t.leftKey = t.leftKey * (-1) where t.leftKey between ?1 and ?2"
            );
            changeLeftToNegative.setParameter(1, movableTree.getLeftKey());
            changeLeftToNegative.setParameter(2, movableTree.getRightKey());
            changeLeftToNegative.executeUpdate();

            Query changeRightToNegative = manager.createQuery(
                    "update Tree t set t.rightKey = t.rightKey * (-1) where t.rightKey between ?1 and ?2"
            );
            changeRightToNegative.setParameter(1, movableTree.getLeftKey());
            changeRightToNegative.setParameter(2, movableTree.getRightKey());
            changeRightToNegative.executeUpdate();

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
            Long newHostTreeId = Long.parseLong(bufferedReader.readLine());

            if (newHostTreeId == 0) {
                TypedQuery<Integer> maxRightKeyTypedQuery = manager.createQuery(
                        "select max(t.rightKey) from Tree t", Integer.class
                );
                List<Integer> maxRightKeyList = maxRightKeyTypedQuery.getResultList();

                Query changeLevels = manager.createQuery(
                        "update Tree t set t.level = t.level - ?1 where t.leftKey < 0"
                );
                changeLevels.setParameter(1, movableTree.getLevel());
                changeLevels.executeUpdate();

                // -2 -3 -> 20 -> 21 22

                // 0 - (-2) - 2 + 20 + 1 = 21
                // 0 - (-3) - 2 + 20 + 1 = 22

                Query changeLeftKeys = manager.createQuery(
                        "update Tree t set t.leftKey = 0 - t.leftKey - ?1 + ?2 + 1 where t.leftKey < 0"
                );
                changeLeftKeys.setParameter(1, movableTree.getLeftKey());
                changeLeftKeys.setParameter(2, maxRightKeyList.get(0));
                changeLeftKeys.executeUpdate();

                Query changeRightKeys = manager.createQuery(
                        "update Tree t set t.rightKey = 0 - t.rightKey - ?1 + ?2 + 1 where t.rightKey < 0"
                );
                changeRightKeys.setParameter(1, movableTree.getLeftKey());
                changeRightKeys.setParameter(2, maxRightKeyList.get(0));
                changeRightKeys.executeUpdate();
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

                Query relocateLeftKeys = manager.createQuery(
                        "update Tree t set t.leftKey = 0 - t.leftKey + ?1 - ?2 - 1 where t.leftKey < 0"
                );
                relocateLeftKeys.setParameter(1, newHostTree.getRightKey());
                relocateLeftKeys.setParameter(2, movableTree.getRightKey());
                relocateLeftKeys.executeUpdate();

                Query relocateRightKeys = manager.createQuery(
                        "update Tree t set t.rightKey = 0 - t.rightKey + ?1 - ?2 - 1 where t.rightKey < 0"
                );
                relocateRightKeys.setParameter(1, newHostTree.getRightKey());
                relocateRightKeys.setParameter(2, movableTree.getRightKey());
                relocateRightKeys.executeUpdate();

                manager.refresh(movableTree);

                Query updateLevel = manager.createQuery(
                        "update Tree t set t.level = abs(t.level - ?4) + ?3 + 1 where t.leftKey between ?1 and ?2"
                );
                updateLevel.setParameter(1, movableTree.getLeftKey());
                updateLevel.setParameter(2, movableTree.getRightKey());
                updateLevel.setParameter(3, newHostTree.getLevel());
                updateLevel.setParameter(4, movableTree.getLevel());
                updateLevel.executeUpdate();
            }

            // -2 -7 -> 5 18 -> 12 17
            // -3 -4            13 14
            // -5 -6            15 16

            // 0 - (-2) + (18 - 7 - 1) = 12
            // 0 - (-7) + (18 - 7 - 1) = 17
            // 0 - (-5) + (18 - 7 - 1) = 15

            // 0 - (<1>) + (<2> - <3> - 1)
            // * `1` - актуальный отрицательный ключ из базы данных.
            // * `2` - положительный правый ключ новой родительской категории после выделения места.
            // * `3` - положительный правый ключ перемещаемой категории.

            // 4 -> 11 -> 12
            // 5          13

            // 4 - 4 + 11 + 1 = 12
            // 5 - 4 + 11 + 1 = 13

            // 13 -> 2 -> 3
            // 14         4

            // 13 - 13 + 2 + 1 = 3
            // 14 - 13 + 2 + 1 = 4

            manager.getTransaction().commit();
        } catch (IOException e) {
            System.out.println("I/O error");
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}
