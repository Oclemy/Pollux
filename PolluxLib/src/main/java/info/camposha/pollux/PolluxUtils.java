package info.camposha.pollux;

import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

/**
 * ANDROID: http://www.camposha.info : Oclemy.
 */

class PolluxUtils {
    public static void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
    public static String generateRandomWords(int numberOfWords)
    {
        String[] randomStrings = new String[numberOfWords];
        Random random = new Random();
        StringBuilder sb=new StringBuilder();
        for(int i = 0; i < numberOfWords; i++)
        {
            char[] word = new char[random.nextInt(8)+3]; // words of length 3 through 10. (1 and 2 letter words are boring.)
            for(int j = 0; j < word.length; j++)
            {
                word[j] = (char)('a' + random.nextInt(26));
            }
            randomStrings[i] = new String(word);
            String cap = randomStrings[i].substring(0, 1).toUpperCase() + randomStrings[i].substring(1);
            sb.append(cap+" ");

        }
        return sb.toString();
    }

}
