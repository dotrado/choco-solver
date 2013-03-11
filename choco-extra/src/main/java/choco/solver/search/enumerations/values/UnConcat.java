/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.solver.search.enumerations.values;

public class UnConcat<A> extends ValueIterator<ValueIterator<A>> {
    ValueIterator<A> h;
    int nbPieces;
    ValueIterator<A>[] hs;

    public UnConcat(ValueIterator<A> h1, int n) {
        h = h1;
        nbPieces = n;
        hs = new ValueIterator[nbPieces];
        int lengthOfAPiece = h.length() / nbPieces;
        for (int i = 0; i < nbPieces; i++) {
            if (i == nbPieces - 1) {
                // the last part can be longer
                hs[i] = new AuxFromTo<A>(h, i * lengthOfAPiece, h.length() - 1);
            } else {
                hs[i] = new AuxFromTo<A>(h, i * lengthOfAPiece, (i + 1) * lengthOfAPiece - 1);
            }
        }
    }

    public ValueIterator<A> get(int i) {
        return hs[i];
    }

    public int length() {
        return hs.length;
    }

    public String toString() {
        String result = "UnConcat(";
        result += h + "," + nbPieces;
        //for (int i=0; i<hs.length-1; i++) {
        //result += hs[i] + ",";
        //}
        //result += hs[hs.length-1];
        return result + ")";
    }
}