class PriorityQueue:
    def __init__(self):
        self.q = []

    def add(self, v):
        if not self.q:
            self.q.append(v)
        else:
            inserted = False
            for i, value in enumerate(self.q):
                if v <= value:
                    self.q.insert(i, v)
                    inserted = True
                    break
            if inserted is False:
                self.q.append(v)

    def pop_front(self):
        self.q = self.q[1:]

    def pop_front(self):
        self.q = self.q[:-1]

    def back(self):
        return self.q[-1]

    def front(self):
        return self.q[0]

    def remove(self, v):
        self.q.remove(v)

    def get_queue(self):
        return self.q