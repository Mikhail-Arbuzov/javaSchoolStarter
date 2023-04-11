package com.digdes.school;

import com.digdes.school.someexceptions.InvalidFieldValueException;
import com.digdes.school.someexceptions.InvalidNameFieldException;
import com.digdes.school.someexceptions.InvalidRequestParametersException;
import com.digdes.school.someexceptions.ParametersException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSchoolStarter {
    private final List<Map<String, Object>> result = new ArrayList<>();

    public JavaSchoolStarter() {
    }

    public List<Map<String, Object>> execute(String request) throws Exception {
        if (request.startsWith("INSERT VALUES") || request.startsWith("insert values")) {
            Map<String, Object> row1 = insert(request);
            result.add(row1);
        } else if (request.startsWith("SELECT") || request.startsWith("select")) {
            select(request, result);

        } else if (request.startsWith("UPDATE VALUES") || request.startsWith("update values")) {
            update(request, result);
        } else if (request.startsWith("DELETE") || request.startsWith("delete")) {
            delete(request, result);
        } else {
            throw new InvalidRequestParametersException("Неверно указана команда (insert/update/select/delete)." +
                    " Все символы должны быть либо в верхнем регистре (INSERT VALUES), либо в нижнем (insert values)");
        }
        return new ArrayList<>();
    }

    //метод выполняющий команду INSERT VALUES
    private Map<String, Object> insert(String exec) throws ParametersException {
        Map<String, Object> row = new HashMap<>();
        //шаблон соответствующий запросу: INSERT VALUES 'lastName' = 'Федоров', 'id' = 3, 'age'= 40, 'active' = true
        String regex = "^INSERT VALUES\\s*(\\'[a-zA-Z]+\\'\\s*=+\\s*['.a-zA-Zа-яА-ЯёЁ0-9]+\\s*\\,?\\s*)+";

        Matcher matcher = getMatcher(regex, exec);
        if (matcher.matches()) {
            String str = exec.trim().substring(13);
            String[] elements = str.split("\\s?\\,\\s?");
            Map<String, Object> map = new HashMap<>();

            //получаем Мар , где ее ключем является - имя колонки, а значением - значение колонки
            for (String v : elements) {
                String[] arr = v.split("\\s?\\=\\s?");
                map.put(arr[0].trim(), arr[1].trim());
            }

            //получаем Мар infoUsers с корректными значениями ее элементов
            Map<String, Object> infoUsers = getCorrectFieldValuesInMap(map);

            //проверяем infoUsers по ее ключам (на наличие колонок).
            // Если конкретная колонка отсутствует, присваеваем ей значения null.
            // Итоговый результат сохраняем в Map<String, Object> row
            if (infoUsers.size() > 0) {
                String[] list = {"id", "lastName", "age", "cost", "active"};
                for (int i = 0; i < list.length; i++) {
                    if (infoUsers.containsKey(list[i])) {
                        row.put(list[i], infoUsers.get(list[i]));
                    } else {
                        row.put(list[i], null);
                    }
                }
            }
        } else {
            throw new InvalidRequestParametersException("некорректно указан запрос!");
        }

        return row;
    }


    //метод выполняющий команду UPDATE VALUES
    private void update(String exec, List<Map<String, Object>> result) throws ParametersException {
        //шаблон соответствующий запросам типа: UPDATE VALUES 'active' = true, 'age' = 46
        String strPattern1 = "^UPDATE VALUES\\s*(\\'[a-zA-Z]+\\'\\s*=+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*\\,?\\s*)+";

        //шаблон соответствующий запросам типа: UPDATE VALUES 'active' = true WHERE 'age'>=30
        String strPattern2 = "^UPDATE VALUES\\s*((\\'[a-zA-Z]+\\'\\s*=+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*\\,?\\s*)+\\s*(where|WHERE)\\s*" +
                "\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)";

        //шаблон соответствующий запросам типа:
        // UPDATE VALUES 'lastName'='Петров','age' = 27 WHERE 'lastName' like 'Пeт%'
        String strPattern3 = "^UPDATE VALUES\\s*((\\'[a-zA-Z]+\\'\\s*=+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*\\,?\\s*)+" +
                "\\s*(where|WHERE)\\s*\\'lastName\\'\\s+(like|ilike)\\s+(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\'|\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\')\\s*)";

        //шаблон соответствующий запросам типа:
        //UPDATE VALUES 'lastName' ='Ivanov','age' = 30 WHERE 'lastName' ilike '%AN%' or 'age'>=30
        String strPattern4 = "^UPDATE VALUES\\s*((\\'[a-zA-Z]+\\'\\s*=+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*\\,?\\s*)+" +
                "\\s*(where|WHERE)\\s*\\'lastName\\'\\s+(like|ilike)\\s+(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]" +
                "+\\'|\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\')\\s+(and|or)+\\s+\\'[a-zA-Z]+" +
                "\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)";

        //шаблон соответствующий запросам типа:
        //UPDATE VALUES 'active' = true where 'age'>=30 and 'lastName' like '%n%'
        String strPattern5 = "^UPDATE VALUES\\s*((\\'[a-zA-Z]+\\'\\s*=+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*\\,?\\s*)+\\s*(where|WHERE)\\s*" +
                "\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+\\'|(true|false|null)|([0-9]+" +
                "([.][0-9]*)?|[.][0-9]+))\\s+(and|or)+\\s+\\'lastName\\'\\s+(like|ilike)\\s+(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\'|\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\')\\s*)";

        //шаблон соответствующий запросам типа:
        ////UPDATE VALUES 'active' = true where 'age'>=30 or 'cost' = 2.5
        String strPattern6 = "^UPDATE VALUES\\s*((\\'[a-zA-Z]+\\'\\s*=+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*\\,?\\s*)+\\s*(where|WHERE)\\s*" +
                "\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s+(and|or)+\\s+\\'[a-zA-Z]+" +
                "\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)";

        Matcher matcher1 = getMatcher(strPattern1, exec);
        Matcher matcher2 = getMatcher(strPattern2, exec);
        Matcher matcher3 = getMatcher(strPattern3, exec);
        Matcher matcher4 = getMatcher(strPattern4, exec);
        Matcher matcher5 = getMatcher(strPattern5, exec);
        Matcher matcher6 = getMatcher(strPattern6, exec);

        if (matcher1.matches()) {
            String st = exec.trim().substring(13);
            Map<String, Object> infoUsers = getMapInfoUsers(st);
            for (Map<String, Object> m : result) {
                for (Map.Entry<String, Object> temp : infoUsers.entrySet()) {
                    m.put(temp.getKey(), temp.getValue());
                }
            }
        } else if (matcher2.matches()) {
            String str = exec.trim().substring(13);
            String[] arr = str.trim().split("(?i)(where)");
            String sign = getComparisonSignFromString(arr[1]);
            String[] arrComparisonValue = arr[1].trim().split("(>=|<=|>|<|!=|=)");
            String strTrim = arrComparisonValue[0].trim().replaceAll("\\'", "");
            String field = getFieldFromString(strTrim);
            Map<String, Object> map = getMapInfoUsers(arr[0]);
            for (Map<String, Object> m : result) {
                for (Map.Entry<String, Object> temp : map.entrySet()) {
                    if (checkComparisonAndEquality(arrComparisonValue, field, sign, m)) {
                        m.put(temp.getKey(), temp.getValue());
                    }
                }
            }

        } else if (matcher3.matches()) {
            String str = exec.trim().substring(13);
            String[] arrChangingValues = str.trim().split("(?i)(where)");
            String word = getOperatorLikeOrIlike(arrChangingValues[1]);
            String[] fragments = arrChangingValues[1].trim().split("(?i)(like|ilike)");
            Map<String, Object> map = getMapInfoUsers(arrChangingValues[0]);
            for (Map<String, Object> m : result) {
                for (Map.Entry<String, Object> temp : map.entrySet()) {
                    if (checkPatternValueByLastName(fragments, word, m)) {
                        m.put(temp.getKey(), temp.getValue());
                    }
                }
            }
        } else if (matcher4.matches()) {
            String str = exec.trim().substring(13);
            String[] arrChangingValues = str.trim().split("(?i)(where)");
            Map<String, Object> infoUsers = getMapInfoUsers(arrChangingValues[0]);
            String operator = getOperatorAndOr(arrChangingValues[1]);
            String[] operations = arrChangingValues[1].trim().split("(?i)(and|or)");
            String[] expressionOne = operations[0].trim().split("(?i)(like|ilike)");
            String[] expressionTwo = operations[1].trim().split("(>=|<=|>|<|!=|=)");
            String word = getOperatorLikeOrIlike(operations[0]);
            String sign = getComparisonSignFromString(operations[1]);
            editSearchResult(result, infoUsers, operator, expressionOne, expressionTwo, word, sign);

        } else if (matcher5.matches()) {
            String st = exec.trim().substring(13);
            String[] arr = st.trim().split("(?i)(where)");
            String operator = getOperatorAndOr(arr[1]);
            String[] expressions = arr[1].trim().split("(?i)(and|or)");
            String[] expressionOne = expressions[0].trim().split("(>=|<=|>|<|!=|=)");
            String[] expressionTwo = expressions[1].trim().split("(?i)(like|ilike)");
            String sign = getComparisonSignFromString(expressions[0]);
            String word = getOperatorLikeOrIlike(expressions[1]);
            Map<String, Object> map = getMapInfoUsers(arr[0]);
            editSearchResult(result, map, operator, expressionTwo, expressionOne, word, sign);

        } else if (matcher6.matches()) {
            String str = exec.trim().substring(13);
            String[] arrChangingValues = str.trim().split("(?i)(where)");
            String operator = getOperatorAndOr(arrChangingValues[1]);
            Map<String, Object> infoUsers = getMapInfoUsers(arrChangingValues[0]);
            String[] operations = arrChangingValues[1].trim().split("(?i)(and|or)");
            String[] operationOne = operations[0].trim().split("(>=|<=|>|<|!=|=)");
            String[] operationTwo = operations[1].trim().split("(>=|<=|>|<|!=|=)");
            String sign1 = getComparisonSignFromString(operations[0]);
            String sign2 = getComparisonSignFromString(operations[1]);
            String strTrim1 = operationOne[0].trim().replaceAll("\\'", "");
            String field1 = getFieldFromString(strTrim1);
            String strTrim2 = operationTwo[0].trim().replaceAll("\\'", "");
            String field2 = getFieldFromString(strTrim2);
            if (operator.equals("and")) {
                for (Map<String, Object> m : result) {
                    for (Map.Entry<String, Object> temp : infoUsers.entrySet()) {
                        if (checkComparisonAndEquality(operationOne, field1, sign1, m) &&
                                checkComparisonAndEquality(operationTwo, field2, sign2, m)) {
                            m.put(temp.getKey(), temp.getValue());
                        }
                    }
                }
            } else if (operator.equals("or")) {
                for (Map<String, Object> m : result) {
                    for (Map.Entry<String, Object> temp : infoUsers.entrySet()) {
                        if (checkComparisonAndEquality(operationOne, field1, sign1, m) ||
                                checkComparisonAndEquality(operationTwo, field2, sign2, m)) {
                            m.put(temp.getKey(), temp.getValue());
                        }
                    }
                }
            }

        } else {
            throw new InvalidRequestParametersException("некорректно указан запрос!");
        }
    }

    /* изменение значения элементов в коллекции List<Map<String, Object>> result
     за счет результата поиска запроса типа
     UPDATE VALUES 'active' = true WHERE 'age'>=30 and 'lastName' like '%n%' или
     UPDATE VALUES 'lastName' ='Петров', 'age' = 27 WHERE 'lastName' ilike '%n%' or 'age'>=30 */
    private void editSearchResult(List<Map<String, Object>> result, Map<String, Object> infoUsers,
                                  String operator, String[] strPattern, String[] strComparison,
                                  String word, String sign) throws ParametersException {
        String strTrim = strComparison[0].trim().replaceAll("\\'", "");
        String field = getFieldFromString(strTrim);

        if (operator.equals("and")) {
            for (Map<String, Object> m : result) {
                for (Map.Entry<String, Object> temp : infoUsers.entrySet()) {
                    if (checkPatternValueByLastName(strPattern, word, m)
                            && checkComparisonAndEquality(strComparison, field, sign, m)) {
                        m.put(temp.getKey(), temp.getValue());
                    }
                }
            }
        } else if (operator.equals("or")) {
            for (Map<String, Object> m : result) {
                for (Map.Entry<String, Object> temp : infoUsers.entrySet()) {
                    if (checkPatternValueByLastName(strPattern, word, m)
                            || checkComparisonAndEquality(strComparison, field, sign, m)) {
                        m.put(temp.getKey(), temp.getValue());
                    }
                }
            }
        }
    }

    /*получаем Map<String, Object> infoUsers
     содержащую элементы с новыми значениями,
     измененными на основании команды UPDATE*/
    private Map<String, Object> getMapInfoUsers(String st) throws ParametersException {
        Map<String, Object> infoUsers = new HashMap<>();
        /*если в запросе UPDATE несколько колонок, которые нужно изменять ('lastName' = 'Петров', 'age' = 27, ..), то
         * получаем Мар, где ее ключем является - имя колонки, а значением - значение колонки,
         * затем осуществляем проверку полученной Мар на корректность значений,
         * благодаря методу getCorrectFieldValuesInMap(map)
         * если в запросе UPDATE нужно обновить одну колонку,
         * то осуществляем проверку на корректность полученных значений
         * и результат сразу заполняем в Map<String, Object> infoUsers*/
        if (st.contains(",")) {
            String[] elements = st.trim().split("\\s?\\,\\s?");
            Map<String, Object> map = new HashMap<>();

            for (String v : elements) {
                String[] arr = v.split("\\s?\\=\\s?");
                map.put(arr[0].trim(), arr[1].trim());
            }
            infoUsers = getCorrectFieldValuesInMap(map);
        } else {
            String[] array = st.trim().split("\\s?\\=\\s?");
            String strTrim = array[0].trim().replaceAll("\\'", "");
            String f = getFieldFromString(strTrim);

            if (f.equals("id") || f.equals("age")) {
                if (array[1].trim().matches("(\\d+|(?i)(null))")) {
                    Long l = !array[1].trim().equalsIgnoreCase("null")
                            ? Long.parseLong(array[1].trim()) : null;
                    infoUsers.put(f, l);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле " + f);
                }
            } else if (f.equals("cost")) {
                if (array[1].trim().matches("([0-9]+([.][0-9]*)?|[.][0-9]+|(?i)(null))")) {
                    Double d = !array[1].trim().equalsIgnoreCase("null") ?
                            Double.parseDouble(array[1].trim()) : null;
                    infoUsers.put(f, d);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле " + f);
                }
            } else if (f.equals("active")) {
                if (array[1].trim().matches("(?i)(true|false|null)")) {
                    Boolean b = !array[1].trim().equalsIgnoreCase("null") ?
                            Boolean.parseBoolean(array[1].trim()) : null;
                    infoUsers.put(f, b);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле " + f);
                }
            } else if (f.equals("lastName")) {
                if (array[1].trim().matches("(\\'[a-zA-Zа-яА-ЯёЁ]+\\'|(?i)(null))")) {
                    String s = array[1].trim().replaceAll("\\'", "");
                    infoUsers.put(f, s);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле " + f);
                }
            } else {
                throw new InvalidNameFieldException(f + " такого поля нет");
            }

        }
        return infoUsers;
    }

    //метод, выполняющий команду DELETE
    private void delete(String exec, List<Map<String, Object>> result) throws ParametersException {
        String strRegx1 = "^DELETE\\s*";

        //шаблон, соответствующий запросам типа: DELETE WHERE 'id'=1
        String strRegx2 = "^DELETE WHERE\\s*(\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)+";

        //шаблон, соответствующий запросам типа: DELETE WHERE 'lastName' like '%n%'
        String strRegx3 = "^DELETE WHERE\\s*(\\'lastName\\'\\s+(like|ilike)\\s+" +
                "(\\'[a-zA-Zа-яА-ЯёЁ]+\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\')\\s*)+";

        //шаблон, соответствующий запросам типа: DELETE WHERE 'lastName' like '%n%' and 'active' = true
        String strRegx4 = "^DELETE WHERE\\s*(\\'lastName\\'\\s+(like|ilike)\\s+" +
                "(\\'[a-zA-Zа-яА-ЯёЁ]+\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\')\\s+(and|or)+\\s+\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+" +
                "\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)+";

        //шаблон, соответствующий запросам типа: DELETE WHERE 'id'= 1 or 'cost' != 3.01
        String strRegx5 = "^DELETE WHERE\\s*(\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s" +
                "+(and|or)+\\s+\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)+";

        //шаблон, соответствующий запросам типа: DELETE WHERE 'cost' != 3.01 or 'lastName' ilike 'n%'
        String strRegx6 = "^DELETE WHERE\\s*(\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+" +
                "\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))" +
                "\\s+(and|or)+\\s+\\'lastName\\'\\s+(like|ilike)\\s+" +
                "(\\'[a-zA-Zа-яА-ЯёЁ]+\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\')\\s*)+";

        Matcher matcher = getMatcher(strRegx1, exec);
        Matcher matcher2 = getMatcher(strRegx2, exec);
        Matcher matcher3 = getMatcher(strRegx3, exec);
        Matcher matcher4 = getMatcher(strRegx4, exec);
        Matcher matcher5 = getMatcher(strRegx5, exec);
        Matcher matcher6 = getMatcher(strRegx6, exec);

        if (matcher.matches()) {
            result.clear();
        } else if (matcher2.matches()) {
            String str = exec.substring(13);
            String sign = getComparisonSignFromString(str);
            String[] fragments = str.trim().split("(>=|<=|>|<|!=|=)");
            String s = fragments[0].trim().replaceAll("\\'", "");
            String field = getFieldFromString(s);

            Iterator<Map<String, Object>> iterator = result.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> m = iterator.next();
                if (checkComparisonAndEquality(fragments, field, sign, m)) {
                    iterator.remove();
                }
            }
        } else if (matcher3.matches()) {
            String st = exec.substring(13);
            String likeOrIlike = getOperatorLikeOrIlike(st);
            String[] elements = st.trim().split("(?i)(like|ilike)");
            Iterator<Map<String, Object>> iterator = result.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> m = iterator.next();
                if (checkPatternValueByLastName(elements, likeOrIlike, m)) {
                    iterator.remove();
                }
            }
        } else if (matcher4.matches()) {
            String str = exec.trim().substring(13);
            String[] subStrs = str.trim().split("(?i)(and|or)");
            String[] strPatterns = subStrs[0].trim().split("(?i)(like|ilike)");
            String[] strComparisons = subStrs[1].trim().split("(>=|<=|>|<|!=|=)");
            String strLikeOrIlike = getOperatorLikeOrIlike(subStrs[0]);
            String sign = getComparisonSignFromString(subStrs[1]);
            deleteSearchResult(result, str, strPatterns, strComparisons, strLikeOrIlike, sign);
        } else if (matcher5.matches()) {
            String s = exec.trim().substring(13);
            String[] fragments = s.trim().split("(?i)(and|or)");
            String sign1 = getComparisonSignFromString(fragments[0]);
            String sign2 = getComparisonSignFromString(fragments[1]);
            String[] comparisons2 = fragments[1].trim().split("(>=|<=|>|<|!=|=)");
            String[] comparisons1 = fragments[0].trim().split("(>=|<=|>|<|!=|=)");
            String strTrim1 = comparisons1[0].trim().replaceAll("\\'", "");
            String strTrim2 = comparisons2[0].trim().replaceAll("\\'", "");
            String f1 = getFieldFromString(strTrim1);
            String f2 = getFieldFromString(strTrim2);
            String op = getOperatorAndOr(s);
            Iterator<Map<String, Object>> iterator = result.iterator();
            if (op.equals("and")) {
                while (iterator.hasNext()) {
                    Map<String, Object> m = iterator.next();
                    if (checkComparisonAndEquality(comparisons1, f1, sign1, m) &&
                            checkComparisonAndEquality(comparisons2, f2, sign2, m)) {
                        iterator.remove();
                    }
                }
            } else if (op.equals("or")) {
                while (iterator.hasNext()) {
                    Map<String, Object> m = iterator.next();
                    if (checkComparisonAndEquality(comparisons1, f1, sign1, m) ||
                            checkComparisonAndEquality(comparisons2, f2, sign2, m)) {
                        iterator.remove();
                    }
                }
            }
        } else if (matcher6.matches()) {
            String str = exec.trim().substring(13);
            String[] subStrs = str.trim().split("(?i)(and|or)");
            String[] strComparisons = subStrs[0].trim().split("(>=|<=|>|<|!=|=)");
            String[] strPatterns = subStrs[1].trim().split("(?i)(like|ilike)");
            String sign = getComparisonSignFromString(subStrs[0]);
            String strLikeOrIlike = getOperatorLikeOrIlike(subStrs[1]);
            deleteSearchResult(result, str, strPatterns, strComparisons, strLikeOrIlike, sign);

        } else {
            throw new InvalidRequestParametersException("некорректно указан запрос!");
        }
    }

    /* удаление элементов из коллекции List<Map<String, Object>> result
    по результатам поиска запроса типа
    DELETE WHERE 'cost'>=3.6 and 'lastName' like 'n%' или
    DELETE WHERE 'lastName' ilike '%n%' or 'age'>=30 */
    private void deleteSearchResult(List<Map<String, Object>> result, String str,
                                    String[] strPatterns, String[] strComparisons,
                                    String strLikeOrIlike, String sign) throws ParametersException {
        String strTrim = strComparisons[0].trim().replaceAll("\\'", "");
        String field = getFieldFromString(strTrim);
        Iterator<Map<String, Object>> iterator = result.iterator();
        String operator = getOperatorAndOr(str);
        if (operator.equals("and")) {
            while (iterator.hasNext()) {
                Map<String, Object> m = iterator.next();
                if (checkPatternValueByLastName(strPatterns, strLikeOrIlike, m) &&
                        checkComparisonAndEquality(strComparisons, field, sign, m)) {
                    iterator.remove();
                }
            }
        } else if (operator.equals("or")) {
            while (iterator.hasNext()) {
                Map<String, Object> m = iterator.next();
                if (checkPatternValueByLastName(strPatterns, strLikeOrIlike, m) ||
                        checkComparisonAndEquality(strComparisons, field, sign, m)) {
                    iterator.remove();
                }
            }
        }
    }

    //метод выполняющий команду SELECT
    private void select(String exec, List<Map<String, Object>> result) throws ParametersException {
        String strPattern1 = "^SELECT\\s*";

        //шаблон соответствующий запросам типа: SELECT WHERE 'id'=1
        String strPattern2 = "^SELECT WHERE\\s*(\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)+";

        //шаблон соответствующий запросам типа: SELECT WHERE 'lastName' like '%n%'
        String strPattern3 = "^SELECT WHERE\\s*(\\'lastName\\'\\s+(like|ilike)\\s+(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\'|\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\')\\s*)+";

        //шаблон соответствующий запросам типа: SELECT WHERE 'lastName' like '%n%' and 'active' = true
        String strPattern4 = "^SELECT WHERE\\s*(\\'lastName\\'\\s+(like|ilike)\\s+(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\'|\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\%\\')\\s+(and|or)+\\s+\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)+";

        //шаблон соответствующий запросам типа: SELECT WHERE 'id'= 1 and 'cost' != 3.01
        String strPattern5 = "^SELECT WHERE\\s*(\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s+(and|or)+\\s+\\'[a-zA-Z]+" +
                "\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s*)+";

        //шаблон соответствующий запросам типа: SELECT WHERE 'cost' != 3.01 or 'lastName' ilike 'n%'
        String strPattern6 = "^SELECT WHERE\\s*(\\'[a-zA-Z]+\\'\\s*(>=|<=|>|<|!=|=)+\\s*(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|(true|false|null)|([0-9]+([.][0-9]*)?|[.][0-9]+))\\s+(and|or)+" +
                "\\s+\\'lastName\\'\\s+(like|ilike)\\s+(\\'[a-zA-Zа-яА-ЯёЁ]+" +
                "\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'|\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\'|\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\')\\s*)+";

        Matcher matcher1 = getMatcher(strPattern1, exec);
        Matcher matcher2 = getMatcher(strPattern2, exec);
        Matcher matcher3 = getMatcher(strPattern3, exec);
        Matcher matcher4 = getMatcher(strPattern4, exec);
        Matcher matcher5 = getMatcher(strPattern5, exec);
        Matcher matcher6 = getMatcher(strPattern6, exec);

        if (matcher1.matches()) {
            System.out.printf("%1$2s  %2$4s %3$5s %4$8s %5$11s", "id", "lastName", "age", "cost", "active");
            for (Map<String, Object> m : result) {
                print(m);
            }

        } else if (matcher2.matches()) {
            String s = exec.trim().substring(13);
            String sign = getComparisonSignFromString(s);
            String[] elements = s.trim().split("(>=|<=|>|<|!=|=)");
            String strTrim = elements[0].trim().replaceAll("\\'", "");
            String field = getFieldFromString(strTrim);
            System.out.printf("%1$2s  %2$4s %3$5s %4$8s %5$11s", "id", "lastName", "age", "cost", "active");
            for (Map<String, Object> m : result) {
                if (checkComparisonAndEquality(elements, field, sign, m)) {
                    print(m);
                }
            }

        } else if (matcher3.matches()) {
            String st = exec.trim().substring(13);
            String word = getOperatorLikeOrIlike(st);
            String[] fragments = st.trim().split("(?i)(like|ilike)");
            System.out.printf("%1$2s  %2$4s %3$5s %4$8s %5$11s", "id", "lastName", "age", "cost", "active");
            for (Map<String, Object> m : result) {
                if (checkPatternValueByLastName(fragments, word, m)) {
                    print(m);
                }
            }

        } else if (matcher4.matches()) {
            String str = exec.trim().substring(13);
            String[] subStrs = str.trim().split("(?i)(and|or)");
            String[] strPatternLike = subStrs[0].trim().split("(?i)(like|ilike)");
            String[] strComparison = subStrs[1].trim().split("(>=|<=|>|<|!=|=)");
            printSearchResult(result, str, strComparison, strPatternLike);

        } else if (matcher5.matches()) {
            String str = exec.trim().substring(13);
            String[] subStrs = str.trim().split("(?i)(and|or)");
            String[] comparison1 = subStrs[0].trim().split("(>=|<=|>|<|!=|=)");
            String[] comparison2 = subStrs[1].trim().split("(>=|<=|>|<|!=|=)");
            String strTrim1 = comparison1[0].trim().replaceAll("\\'", "");
            String strTrim2 = comparison2[0].trim().replaceAll("\\'", "");
            String field1 = getFieldFromString(strTrim1);
            String field2 = getFieldFromString(strTrim2);
            String sign1 = getComparisonSignFromString(subStrs[0]);
            String sign2 = getComparisonSignFromString(subStrs[1]);
            System.out.printf("%1$2s  %2$4s %3$5s %4$8s %5$11s", "id", "lastName", "age", "cost", "active");
            String operator = getOperatorAndOr(str);
            if (operator.equals("and")) {
                for (Map<String, Object> m : result) {
                    if (checkComparisonAndEquality(comparison1, field1, sign1, m)
                            && checkComparisonAndEquality(comparison2, field2, sign2, m)) {
                        print(m);
                    }
                }
            } else if (operator.equals("or")) {
                for (Map<String, Object> m : result) {
                    if (checkComparisonAndEquality(comparison1, field1, sign1, m)
                            || checkComparisonAndEquality(comparison2, field2, sign2, m)) {
                        print(m);
                    }
                }
            }

        } else if (matcher6.matches()) {
            String str = exec.trim().substring(13);
            String[] fragments = str.trim().split("(?i)(and|or)");
            String[] strComparison = fragments[0].trim().split("(>=|<=|>|<|!=|=)");
            String[] strPatternLike = fragments[1].trim().split("(?i)(like|ilike)");
            printSearchResult(result, str, strComparison, strPatternLike);
        } else {
            throw new InvalidRequestParametersException("некорректно указан запрос!");
        }
    }

    /*вывод в консоль результата поиска запроса типа
    SELECT WHERE 'age'>=30 and 'lastName' like '%n%' или
    SELECT WHERE 'lastName' ilike '%n%' or 'age'>=30 */
    private void printSearchResult(List<Map<String, Object>> result, String str,
                                   String[] strComparison, String[] strPatternLike) throws ParametersException {
        String likeOrIlike = getOperatorLikeOrIlike(str);
        String symbol = getComparisonSignFromString(str);
        String strTrim = strComparison[0].trim().replaceAll("\\'", "");
        String field = getFieldFromString(strTrim);
        System.out.printf("%1$2s  %2$4s %3$5s %4$8s %5$11s", "id", "lastName", "age", "cost", "active");
        String op = getOperatorAndOr(str);
        if (op.equals("and")) {
            for (Map<String, Object> m : result) {
                if (checkComparisonAndEquality(strComparison, field, symbol, m)
                        && checkPatternValueByLastName(strPatternLike, likeOrIlike, m)) {
                    print(m);
                }
            }
        } else if (op.equals("or")) {
            for (Map<String, Object> m : result) {
                if (checkComparisonAndEquality(strComparison, field, symbol, m)
                        || checkPatternValueByLastName(strPatternLike, likeOrIlike, m)) {
                    print(m);
                }
            }
        }
    }

    //Получаем совпадение строки запроса с регулярным выражением, независимо от регистра
    private Matcher getMatcher(String strPattern, String exc) {
        Pattern pattern = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(exc);
        return matcher;
    }

    //Получаем знак сравнения из строки запроса
    private String getComparisonSignFromString(String str) {
        String symbol = "";
        if (str.contains(">=")) {
            symbol = ">=";
        } else if (str.contains("<=")) {
            symbol = "<=";
        } else if (str.contains(">")) {
            symbol = ">";
        } else if (str.contains("<")) {
            symbol = "<";
        } else if (str.contains("!=")) {
            symbol = "!=";
        } else if (str.contains("=")) {
            symbol = "=";
        }
        return symbol;
    }

    //Получаем оператор AND или OR из строки запроса
    private String getOperatorAndOr(String st) {
        //между операторами AND или OR должны быть пробелы в запросе
        Pattern pattern = Pattern.compile("(?i)(\\s+(and)\\s+)");
        Matcher match = pattern.matcher(st);
        Pattern pattern2 = Pattern.compile("(?i)(\\s+(or)\\s+)");
        Matcher match2 = pattern2.matcher(st);
        String operator = "";
        if (match.find()) {
            operator = "and";
        } else if (match2.find()) {
            operator = "or";
        }

        return operator;
    }

    //Получаем оператор LIKE или ILIKE из строки запроса,независимо от регистра
    private String getOperatorLikeOrIlike(String str) {
        //между операторами LIKE или ILIKE должны быть пробелы в запросе
        Pattern pattern = Pattern.compile("(?i)(\\s+(like)\\s+)");
        Matcher match = pattern.matcher(str);
        Pattern pattern2 = Pattern.compile("(?i)(\\s+(ilike)\\s+)");
        Matcher match2 = pattern2.matcher(str);
        String operatorLIkeOrIlike = "";
        if (match.find()) {
            operatorLIkeOrIlike = "like";
        } else if (match2.find()) {
            operatorLIkeOrIlike = "ilike";
        }

        return operatorLIkeOrIlike;
    }

    //Получаем поле(колонку), независимо от регистра, из строки
    private String getFieldFromString(String str) {
        String field = "";
        if (str.equalsIgnoreCase("id")) {
            field = "id";
        } else if (str.equalsIgnoreCase("lastname")) {
            field = "lastName";
        } else if (str.equalsIgnoreCase("age")) {
            field = "age";
        } else if (str.equalsIgnoreCase("cost")) {
            field = "cost";
        } else if (str.equalsIgnoreCase("active")) {
            field = "active";
        }
        return field;
    }

    //Получаем Map<String, Object>, с элементами, содержащими корректные колонки с их значениями
    private Map<String, Object> getCorrectFieldValuesInMap(Map<String, Object> map) throws ParametersException {
        Map<String, Object> maps = new HashMap<>();
        for (Map.Entry<String, Object> mp : map.entrySet()) {
            if (mp.getKey().equalsIgnoreCase("'id'")) {
                if (mp.getValue().toString().matches("(\\d+|(?i)(null))")) {
                    Long i = !mp.getValue().toString().equalsIgnoreCase("null") ?
                            Long.parseLong(mp.getValue().toString()) : null;
                    maps.put("id", i);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле id");
                }
            } else if (mp.getKey().equalsIgnoreCase("'lastname'")) {
                String s = mp.getValue().toString().replaceAll("\\'", "");
                if (s.matches("([a-zA-Zа-яА-ЯёЁ]+|(?i)(null))")) {
                    maps.put("lastName", s);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле lastName");
                }
            } else if (mp.getKey().equalsIgnoreCase("'age'")) {
                if (mp.getValue().toString().matches("(\\d+|(?i)(null))")) {
                    Long i = !mp.getValue().toString().equalsIgnoreCase("null") ?
                            Long.parseLong(mp.getValue().toString()) : null;
                    maps.put("age", i);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле age");
                }
            } else if (mp.getKey().equalsIgnoreCase("'cost'")) {
                if (mp.getValue().toString().matches("([0-9]+([.][0-9]*)?|[.][0-9]+|(?i)(null))")) {
                    Double d = !mp.getValue().toString().equalsIgnoreCase("null") ?
                            Double.parseDouble(mp.getValue().toString()) : null;
                    maps.put("cost", d);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле cost");
                }
            } else if (mp.getKey().equalsIgnoreCase("'active'")) {
                if (mp.getValue().toString().matches("(?i)(true|false|null)")) {
                    Boolean b = !mp.getValue().toString().equalsIgnoreCase("null") ?
                            Boolean.parseBoolean(mp.getValue().toString()) : null;
                    maps.put("active", b);
                } else {
                    throw new InvalidFieldValueException("неверно передано значение в поле active");
                }
            } else {
                throw new InvalidNameFieldException(mp.getKey() + " такого поля нет");
            }
        }

        return maps;
    }

    /* метод, выполняющий проверку поиска элементов коллекции Map<String, Object> map,
    по значениям колонок, указанных в запросе (например 'age'>=30),
    на основании операции (=|!=|>|<|>=|<=).Результатом проверки является boolean значение */
    private boolean checkComparisonAndEquality(String[] arr, String field, String symbol,
                                               Map<String, Object> map) throws ParametersException {
        boolean flag = false;

        if (!arr[1].trim().equals("null") && !arr[1].trim().equals("NULL")) {
            if (arr[1].trim().matches("\\d+") && field.equals("id")
                    || arr[1].trim().matches("\\d+") && field.equals("age")
                    || arr[1].trim().matches("([0-9]+([.][0-9]*)?|[.][0-9]+)")
                    && field.equals("cost")) {
                switch (symbol) {
                    case ">":
                        if ((map.get(field) instanceof Long)) {
                            flag = (Long) (map.get(field)) > Long.parseLong(arr[1].trim());
                        } else if ((map.get(field) instanceof Double)) {
                            flag = (Double) (map.get(field)) > Double.parseDouble(arr[1].trim());
                        }
                        break;
                    case "<":
                        if ((map.get(field) instanceof Long)) {
                            flag = (Long) (map.get(field)) < Long.parseLong(arr[1].trim());
                        } else if ((map.get(field) instanceof Double)) {
                            flag = (Double) (map.get(field)) < Double.parseDouble(arr[1].trim());
                        }
                        break;
                    case ">=":
                        if ((map.get(field) instanceof Long)) {
                            flag = (Long) (map.get(field)) >= Long.parseLong(arr[1].trim());
                        } else if ((map.get(field) instanceof Double)) {
                            flag = (Double) (map.get(field)) >= Double.parseDouble(arr[1].trim());
                        }
                        break;
                    case "<=":
                        if ((map.get(field) instanceof Long)) {
                            flag = (Long) (map.get(field)) <= Long.parseLong(arr[1].trim());
                        } else if ((map.get(field) instanceof Double)) {
                            flag = (Double) (map.get(field)) <= Double.parseDouble(arr[1].trim());
                        }
                        break;
                    case "=":
                        if ((map.get(field) instanceof Long)) {
                            flag = (Long) (map.get(field)) == Long.parseLong(arr[1].trim());
                        } else if ((map.get(field) instanceof Double)) {
                            flag = (Double) (map.get(field)) == Double.parseDouble(arr[1].trim());
                        }
                        break;
                    case "!=":
                        if ((map.get(field) instanceof Long)) {
                            flag = (Long) (map.get(field)) != Long.parseLong(arr[1].trim());
                        } else if ((map.get(field) instanceof Double)) {
                            flag = (Double) (map.get(field)) != Double.parseDouble(arr[1].trim());
                        }
                        break;

                }
            } else if (arr[1].trim().matches("\\'[a-zA-Zа-яА-ЯёЁ]+\\'")
                    && field.equals("lastName")) {
                String v = arr[1].trim().replaceAll("\\'", "");
                if (symbol.equals("=")) {
                    flag = map.get(field).equals(v);
                } else if (symbol.equals("!=")) {
                    flag = !(map.get(field).equals(v));
                } else {
                    System.out.println();
                    throw new InvalidRequestParametersException(field + " " + symbol + " " + arr[1].trim() +
                            " .Некорректный запрос!");
                }
            } else if (arr[1].trim().matches("(true|false|TRUE|FALSE)")
                    && field.equals("active")) {
                if (symbol.equals("=")) {
                    flag = (Boolean) (map.get(field)) == Boolean.parseBoolean(arr[1].trim());
                } else if (symbol.equals("!=")) {
                    flag = (Boolean) (map.get(field)) != Boolean.parseBoolean(arr[1].trim());
                } else {
                    System.out.println();
                    throw new InvalidRequestParametersException(field + " " + symbol + " " + arr[1].trim() +
                            " .Некорректный запрос!");
                }
            } else {
                System.out.println();
                throw new InvalidFieldValueException("неверно указано значение для поля " + field);
            }
        } else {
            if (symbol.equals("=")) {
                flag = map.get(field) == null;
            } else if (symbol.equals("!=")) {
                flag = map.get(field) != null;
            } else {
                System.out.println();
                throw new InvalidRequestParametersException(arr[0] + " " + symbol + " null. " +
                        "Значения, которые передаются на сравнения не могут быть null!");
            }
        }
        return flag;
    }

    /* метод, выполняющий проверку поиска элементов коллекции Map<String, Object> map,
    по значениям колонки lastName, указанных в запросе (например 'lastName' like '%n%'),
    на основании операции поиска по шаблонам с учетом регистра (like) и без учета регистра (ilike).
    Результатом проверки является boolean значение */
    private boolean checkPatternValueByLastName(String[] arr, String symbol, Map<String, Object> map) {
        boolean flag = false;
        String value1 = arr[1].trim().replaceAll("\\'", "");
        String value2 = arr[1].trim().replaceAll("(\\'\\%|\\%\\')", "");
        String value3 = arr[1].trim().replaceAll("(\\'\\%|\\')", "");
        String value4 = arr[1].trim().replaceAll("(\\'|\\%\\')", "");

        if (symbol.equals("like")) {
            if (arr[1].trim().matches("\\'[a-zA-Zа-яА-ЯёЁ]+\\'")) {
                flag = map.get("lastName").equals(value1);
            } else if (arr[1].trim().matches("\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'")) {
                flag = map.get("lastName").toString().contains(value2);
            } else if (arr[1].trim().matches("\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\'")) {
                flag = map.get("lastName").toString().endsWith(value3);
            } else if (arr[1].trim().matches("\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\'")) {
                flag = map.get("lastName").toString().startsWith(value4);
            }
        } else {
            if (arr[1].trim().matches("\\'[a-zA-Zа-яА-ЯёЁ]+\\'")) {
                flag = map.get("lastName").toString().equalsIgnoreCase(value1);
            } else if (arr[1].trim().matches("\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\%\\'")) {
                flag = map.get("lastName").toString().toLowerCase().contains(value2.toLowerCase());
            } else if (arr[1].trim().matches("\\'\\%[a-zA-Zа-яА-ЯёЁ]+\\'")) {
                flag = map.get("lastName").toString().toLowerCase().endsWith(value3.toLowerCase());
            } else if (arr[1].trim().matches("\\'[a-zA-Zа-яА-ЯёЁ]+\\%\\'")) {
                flag = map.get("lastName").toString().toLowerCase().startsWith(value4.toLowerCase());
            }
        }

        return flag;
    }

    //вывод в консоль результата команды SELECT
    private void print(Map<String, Object> map) {
        //если колонка имеет значение null, то показываем пустоту
        String id = map.get("id") != null ? map.get("id").toString() : "    ";
        String lastName = map.get("lastName") != null ? map.get("lastName").toString() : "    ";
        String age = map.get("age") != null ? map.get("age").toString() : "    ";
        String cost = map.get("cost") != null ? map.get("cost").toString() : "    ";
        String active = map.get("active") != null ? map.get("active").toString() : "    ";

        System.out.printf("\n%1$2s   %2$4s %3$5s  %4$8s %5$10s", id, lastName, age, cost, active);
    }

}
