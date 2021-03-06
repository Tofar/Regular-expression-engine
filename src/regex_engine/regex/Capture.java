package regex_engine.regex;

import regex_engine.compile.Compile;
import regex_engine.parse.Parse;
import regex_engine.parse.SyntaxError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Capture {
    private String regex;
    private StateMatch match;
    private ArrayList<String> captures = new ArrayList<>();

    public Capture(String r) throws SyntaxError{
        regex = r;
        match = new StateMatch(Compile.compile(Parse.parse(regex)));
    }
    public ArrayList<String> matchAndCapture(String input) throws SyntaxError {
        boolean isfinalstate = true;
        for (int i = 0; i < input.length(); i++) {
            int judge = judgeFunction(0);
            if (judge == 0) {
                if (!match.accept(input.charAt(i))) {
                    isfinalstate = false;
                    break;
                }
            } else if (judge == 1) {
                if (!acceptZeroOrMore(input.substring(i)))
                    isfinalstate = false;
                break;
            } else if (judge == 2) {
                if (!acceptZeroOrOne(input.substring(i)))
                    isfinalstate = false;
                break;
            } else if (judge == 3) {
                if (!acceptOneOrMore(input.substring(i)))
                    isfinalstate = false;
                break;
            } else if (judge == 4) {
                HashMap<Boolean, Integer> capture = match.captureString(input.substring(i));

                if (capture.containsKey(true)) {
                    captures.add(input.substring(i, i + capture.get(true)));
                    i = i + capture.get(true) - 1;

                } else{
                    isfinalstate = false;
                    break;
                }
            }else if (judge == 5) {
                if (!acceptOneCharRange(input.substring(i)))
                    isfinalstate = false;
                break;
            } else
                throw new SyntaxError("The engine has some problems.");
        }

        if (isfinalstate && match.isOnFinalState())
            isfinalstate = true;
//        boolean result = match.isOnFinalState();
        match.reset();
        if (isfinalstate){
            return captures;
        } else {
            throw new SyntaxError("The regex doesn't match your string.");
        }
    }

    private int judgeFunction(int f) throws SyntaxError {
        int min = (Integer) match.getCurrentStates().toArray()[0];
        HashMap<String, HashSet<Integer>> state = match.getStateChart().getConnections().get(min);
        if (f == 0) {
            if (state.containsKey("**"))
                return 1;
            else if (state.containsKey("??"))
                return 2;
            else if (state.containsKey("++"))
                return 3;
            else if (state.containsKey("{}"))
                return 5;
            else if (state.containsKey("()"))
                return 4;
            else
                return 0;
        } else if (f == 1) {
            if (state.containsKey("??"))
                return 2;
            else if (state.containsKey("++"))
                return 3;
            else if (state.containsKey("{}"))
                return 5;
            else if (state.containsKey("()"))
                return 4;
            else
                return 0;
        } else if (f == 2) {
            if (state.containsKey("**"))
                return 1;
            else if (state.containsKey("++"))
                return 3;
            else if (state.containsKey("{}"))
                return 5;
            else if (state.containsKey("()"))
                return 4;
            else
                return 0;
        } else if (f == 3) {
            if (state.containsKey("**"))
                return 1;
            else if (state.containsKey("??"))
                return 2;
            else if (state.containsKey("{}"))
                return 5;
            else if (state.containsKey("()"))
                return 4;
            else
                return 0;
        } else if (f == 5) {
            if (state.containsKey("**"))
                return 1;
            else if (state.containsKey("??"))
                return 2;
            else if (state.containsKey("++"))
                return 3;
            else if (state.containsKey("()"))
                return 4;
            else
                return 0;
        } else
            throw new SyntaxError("The engine has some ploblems.");

    }

    // 匹配 ？
    public boolean acceptZeroOrOne(String input) throws SyntaxError {
//        HashSet<Integer> currentStates2 = new HashSet<>(currentStates);

        int bound = (Integer) match.getStateChart().getConnections().get((Integer) match.getCurrentStates().toArray()[0]).get("??").toArray()[0];
        int times = 0;
        for (int i = 0; i < input.length(); i++) {
            int judge;
            if (i == 0)
                judge = judgeFunction(2);
            else
                judge = judgeFunction(0);

            if (judge == 0) {
                if (!match.accept(input.charAt(i))) {
                    if (times == 0) {
                        match.getCurrentStates().clear();
                        match.getCurrentStates().add(bound);
                        i = -1;
                        times++;
                    } else
                        break;

                }
            } else if (judge == 1) {
                if (times != 0 && !acceptZeroOrMore(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 2) {
                if (times != 0 && !acceptZeroOrOne(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 3) {
                if (times != 0 && !acceptOneOrMore(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 4) {
                HashMap<Boolean, Integer> capture = match.captureString(input.substring(i));
                if (capture.containsKey(false)) {
                    if (times == 0) {
                        match.getCurrentStates().clear();
                        match.getCurrentStates().add(bound);
                        i = -1;
                        times++;
                    } else
                        break;
                } else {
                    captures.add(input.substring(i, i + capture.get(true)));
                    i = i + capture.get(true) - 1;
                }
            } else if (judge == 5) {
                if (times != 0) {
                    acceptOneCharRange(input.substring(i));
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else
                throw new SyntaxError("The engine has some problems.");

        }
        return match.isOnFinalState();
    }

    // 匹配{}  暂时只支持 字符
    public boolean acceptOneCharRange(String input) throws SyntaxError {
        int lower_bound = (Integer) match.getStateChart().getConnections().get(match.getCurrentStates().toArray()[0]).get("}}").toArray()[0];
        int upper_bound = (Integer) match.getStateChart().getConnections().get(match.getCurrentStates().toArray()[0]).get("}}").toArray()[0];
        if (match.getStateChart().getConnections().get(match.getCurrentStates().toArray()[0]).get("}}").toArray().length > 1) {
            int temp = (Integer) match.getStateChart().getConnections().get(match.getCurrentStates().toArray()[0]).get("}}").toArray()[1];
            if (temp > lower_bound)
                upper_bound = temp;
            else
                lower_bound = temp;
        }
        int currentstate = (Integer) match.getCurrentStates().toArray()[0];
        int times = 0;
        for (int i = 0; i < input.length(); i++) {
            int judge;
            if (times < lower_bound)
                judge = judgeFunction(5);
            else
                judge = judgeFunction(0);

            if (judge == 0) {
                if (!match.accept(input.charAt(i))) {
                    if (times < lower_bound || times > upper_bound) {
                        break;
                    } else {
                        i--;
                        if (upper_bound == Integer.MAX_VALUE)
                            times = input.length();
                        else
                            times = upper_bound + 1;
                        match.getCurrentStates().clear();
                        match.getCurrentStates().add(currentstate);
                    }
                } else {
                    times++;
                    if (times < lower_bound) {
                        match.getCurrentStates().clear();
                        match.getCurrentStates().add(currentstate);

                    }
                }
            } else if (judge == 1) {
                if (!acceptZeroOrMore(input.substring(i)))

                    break;
            } else if (judge == 2) {
                if (!acceptZeroOrOne(input.substring(i)))

                    break;
            } else if (judge == 3) {
                if (!acceptOneOrMore(input.substring(i)))

                    break;
            } else if (judge == 4) {
                HashMap<Boolean, Integer> capture = match.captureString(input.substring(i));
                int concat_state = match.acceptConcat(input.substring(i));
                if (capture.containsKey(false)) {
                    if (times < lower_bound || times > upper_bound) {
                        break;
                    } else {
                        i--;
                        if (upper_bound == Integer.MAX_VALUE)
                            times = input.length();
                        else
                            times = upper_bound + 1;
                        match.getCurrentStates().clear();
                        match.getCurrentStates().add((Integer) match.getStateChart().getConnections().get(currentstate).get("()").toArray()[0]);
                    }
                } else {
                    times++;
                    if (times < lower_bound) {
                        match.getCurrentStates().clear();
                        match.getCurrentStates().add(currentstate);
                    }
                    captures.add(input.substring(i, i + capture.get(true)));
                    i = i + concat_state - 1;
                }

            } else
                throw new SyntaxError("don't support it");

        }
        if (times < lower_bound)
            return false;
        else
            return match.isOnFinalState();
    }

    // 匹配 +
    public boolean acceptOneOrMore(String input) throws SyntaxError {
        int currentstate = (Integer) match.getCurrentStates().toArray()[0];
        int times = 0;
        boolean is_match = false;
        for (int i = 0; i < input.length(); i++) {
            int judge;
            if (!is_match)
                judge = judgeFunction(3);
            else
                judge = judgeFunction(0);

            if (judge == 0) {
                if (!match.accept(input.charAt(i))) {
                    if (times < 1) {
                        break;
                    } else {
                        i = i - 1;
                        is_match = true;
                        match.getCurrentStates().clear();
                        match.getCurrentStates().add((Integer) match.getStateChart().getConnections().get(currentstate).get("++").toArray()[0]);
                    }
                } else {
                    if (!is_match) {
                        times++;
                        if (times >= input.length()) {
                            break;
                        } else {
                            match.getCurrentStates().clear();
                            match.getCurrentStates().add(currentstate);
//                            i--;
                        }
                    }
                }
            } else if (judge == 1) {
                if (!is_match && !acceptZeroOrMore(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 2) {
                if (!is_match && !acceptZeroOrOne(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 3) {
                if (!is_match && !acceptOneOrMore(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 4) {
                HashMap<Boolean, Integer> capture = match.captureString(input.substring(i));
                if (capture.containsKey(false)) {
                    if (times < 1) {
                        break;
                    } else {
                        i = i - 1;
                        is_match = true;
                        match.getCurrentStates().clear();
                        match.getCurrentStates().add((Integer) match.getStateChart().getConnections().get(currentstate).get("++").toArray()[0]);
                    }
                } else {
                    captures.add(input.substring(i, i + capture.get(true)));
                    i = i + capture.get(true) - 1;
                    if (!is_match) {
                        times++;
                        if (times >= input.length()) {
                            break;
                        } else {
                            match.getCurrentStates().clear();
                            match.getCurrentStates().add(currentstate);
                        }
                    }
                }

            } else if (judge == 5) {
                if (is_match) {
                    acceptOneCharRange(input.substring(i));
                    break;
                } else
                    throw new SyntaxError("Invalid syntax");
            } else
                throw new SyntaxError("don't support it");

        }
        return match.isOnFinalState();
    }

    // 匹配 *
    public boolean acceptZeroOrMore(String input) throws SyntaxError {
        int currentstate = (Integer) match.getCurrentStates().toArray()[0];
        boolean is_match = false;
        int times = 0;
        for (int i = 0; i < input.length(); i++) {
            int judge;
            if (!is_match)
                judge = judgeFunction(1);
            else
                judge = judgeFunction(0);

            if (judge == 0) {
                if (!match.accept(input.charAt(i))) {
                    i = i - 1;
                    is_match = true;
                    match.getCurrentStates().clear();
                    match.getCurrentStates().add((Integer) match.getStateChart().getConnections().get(currentstate).get("**").toArray()[0]);
                } else {
                    times++;
                    if (!is_match) {
                        if (times >= input.length()) {
                            break;
                        } else {
                            match.getCurrentStates().clear();
                            match.getCurrentStates().add(currentstate);
//                            i--;
                        }
                    }
                }
            } else if (judge == 1) {
                if (!is_match && !acceptZeroOrMore(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 2) {
                if (!is_match && !acceptZeroOrOne(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 3) {
                if (!is_match && !acceptOneOrMore(input.substring(i))) {
                    break;
                } else
                    throw new SyntaxError("Invalid syntax.");
            } else if (judge == 4) {
                HashMap<Boolean, Integer> capture = match.captureString(input.substring(i));
                if (capture.containsKey(false)) {
                    i = i - 1;
                    is_match = true;
                    match.getCurrentStates().clear();
                    match.getCurrentStates().add((Integer) match.getStateChart().getConnections().get(currentstate).get("()").toArray()[0]);
                } else {
                    captures.add(input.substring(i, i + capture.get(true)));
                    i = i + capture.get(true) - 1;
                    if (!is_match) {
                        times++;
                        if (times >= input.length()) {
                            break;
                        } else {
                            match.getCurrentStates().clear();
                            match.getCurrentStates().add(currentstate);
//                            i--;
                        }
                    }
                }
            } else if (judge == 5) {
                if (is_match) {
                    acceptOneCharRange(input.substring(i));
                    break;
                } else
                    throw new SyntaxError("Invalid syntax");
            } else
                throw new SyntaxError("don't support it");

        }
        return match.isOnFinalState();
    }
}
